from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime, timezone, date
from pathlib import Path
import json
import uuid


app = FastAPI(
    title="Ethics as a Service - EaaS",
    description="Independent service for ethical evaluation of data-based tourism recommendations.",
    version="1.2.0"
)


POLICIES_DIR = Path("policies")
AUDIT_DIR = Path("audit")
AUDIT_FILE = AUDIT_DIR / "audit_log.jsonl"

AUDIT_DIR.mkdir(exist_ok=True)


# -----------------------------
# Request models
# -----------------------------

class UserContext(BaseModel):
    mobility: Optional[str] = None
    ageGroup: Optional[str] = None
    preference: Optional[str] = None
    userGroup: Optional[str] = None
    location: Optional[str] = None


class CandidateItem(BaseModel):
    """
    Mirrors the DaaS TourismPlace JSON.

    declaredRiskLevel / declaredRisk are received only to demonstrate that
    EaaS ignores risk declared by DaaS or by the caller.
    """

    id: str
    name: str

    location: Optional[str] = None
    category: Optional[str] = None
    sameAs: Optional[str] = None

    # Declared by DaaS/dataset, ignored by EaaS decision logic.
    declaredRiskLevel: Optional[str] = None

    # Optional alias, useful when a caller sends declaredRisk inside the item.
    declaredRisk: Optional[str] = None

    # Accessibility evidence.
    wheelchairAccessible: Optional[bool] = None
    stepFreeAccess: Optional[bool] = None
    accessibleToilets: Optional[bool] = None

    # Sustainability evidence.
    publicTransportAvailable: Optional[bool] = None
    visitorPressure: Optional[str] = None
    environmentalSensitivity: Optional[str] = None
    localCommunityImpact: Optional[str] = None
    sustainabilityScore: Optional[int] = None

    # Provenance / data quality.
    dataSource: Optional[str] = None
    dataLicense: Optional[str] = None
    lastUpdated: Optional[str] = None


class EthicsRequest(BaseModel):
    operation: str
    userContext: UserContext
    candidateItems: List[CandidateItem]

    # Optional top-level declared risk, also ignored.
    declaredRisk: Optional[str] = Field(
        default=None,
        description="Ignored by EaaS. Risk is calculated internally from policies."
    )


# -----------------------------
# Response models: compact client response
# -----------------------------

class CompactItemEvaluation(BaseModel):
    id: str
    name: str
    riskLevel: str
    decision: str
    declaredRiskIgnored: Optional[str]
    triggeredRules: List[str]
    keyEvidence: Dict[str, Any]


class EthicsResponse(BaseModel):
    evaluationId: str
    timestamp: str
    operation: str
    riskLevel: str
    decision: str
    summary: str
    declaredRiskIgnored: Dict[str, Any]
    appliedPolicies: List[str]
    requiredActions: List[str]
    items: List[CompactItemEvaluation]
    auditRecord: str


# -----------------------------
# Utility functions
# -----------------------------

def load_policies() -> List[Dict[str, Any]]:
    policies = []

    if not POLICIES_DIR.exists():
        raise HTTPException(status_code=500, detail="Policies directory not found.")

    for file in sorted(POLICIES_DIR.glob("*.json")):
        with open(file, "r", encoding="utf-8") as f:
            policies.append(json.load(f))

    return policies


def risk_rank(risk: str) -> int:
    ranking = {
        "LOW": 1,
        "MEDIUM": 2,
        "HIGH": 3,
        "CRITICAL": 4
    }
    return ranking.get(str(risk).upper(), 0)


def decision_rank(decision: str) -> int:
    ranking = {
        "PROCEED": 1,
        "REVISE": 2,
        "ESCALATE": 3,
        "REJECT": 4
    }
    return ranking.get(str(decision).upper(), 0)


def max_risk(risks: List[str]) -> str:
    if not risks:
        return "LOW"
    return str(max(risks, key=risk_rank)).upper()


def max_decision(decisions: List[str]) -> str:
    if not decisions:
        return "PROCEED"
    return str(max(decisions, key=decision_rank)).upper()


def unique_preserve_order(values: List[str]) -> List[str]:
    seen = set()
    result = []

    for value in values:
        if value and value not in seen:
            seen.add(value)
            result.append(value)

    return result


def parse_iso_date(value: Optional[str]) -> Optional[date]:
    if not value:
        return None

    try:
        return date.fromisoformat(value)
    except ValueError:
        return None


def normalize_text(value: Any) -> str:
    if value is None:
        return ""
    return str(value).strip().lower()


def is_missing(value: Any) -> bool:
    if value is None:
        return True

    if isinstance(value, str):
        return value.strip() == ""

    if isinstance(value, list) or isinstance(value, dict):
        return len(value) == 0

    return False


def equals(actual: Any, expected: Any) -> bool:
    if isinstance(actual, bool) or isinstance(expected, bool):
        return actual is expected

    return normalize_text(actual) == normalize_text(expected)


def contains(actual: Any, expected: Any) -> bool:
    return normalize_text(expected) in normalize_text(actual)


def get_declared_item_risk(item: CandidateItem) -> Optional[str]:
    return item.declaredRiskLevel or item.declaredRisk


def get_item_attr(item: CandidateItem, attr: str) -> Any:
    mapping = {
        "id": item.id,
        "name": item.name,
        "location": item.location,
        "category": item.category,
        "same_as": item.sameAs,
        "declared_risk_level": item.declaredRiskLevel,
        "declared_risk": item.declaredRisk,

        "wheelchair_accessible": item.wheelchairAccessible,
        "step_free_access": item.stepFreeAccess,
        "accessible_toilets": item.accessibleToilets,

        "public_transport_available": item.publicTransportAvailable,
        "visitor_pressure": item.visitorPressure,
        "environmental_sensitivity": item.environmentalSensitivity,
        "local_community_impact": item.localCommunityImpact,
        "sustainability_score": item.sustainabilityScore,

        "data_source": item.dataSource,
        "data_license": item.dataLicense,
        "last_updated": item.lastUpdated,

        # Backward-friendly aliases for older policy names.
        "source": item.dataSource,
        "license": item.dataLicense,
    }

    return mapping.get(attr)


def condition_matches(
    condition: Dict[str, Any],
    request: EthicsRequest,
    item: CandidateItem
) -> bool:
    """
    Simple policy condition matcher.
    """

    for key, expected_value in condition.items():

        if key == "operation":
            if not equals(request.operation, expected_value):
                return False

        elif key == "user_mobility":
            if not equals(request.userContext.mobility, expected_value):
                return False

        elif key == "user_age_group":
            if not equals(request.userContext.ageGroup, expected_value):
                return False

        elif key == "user_preference":
            if not equals(request.userContext.preference, expected_value):
                return False

        elif key == "user_group":
            if not equals(request.userContext.userGroup, expected_value):
                return False

        elif key == "user_location":
            if not equals(request.userContext.location, expected_value):
                return False

        elif key.startswith("item_") and key.endswith("_required"):
            attr = key.removeprefix("item_").removesuffix("_required")
            actual = get_item_attr(item, attr)
            missing = is_missing(actual)

            # In this policy language, "_required": true means:
            # trigger this rule only when the required field is missing.
            if expected_value is True:
                if not missing:
                    return False
            elif expected_value is False:
                if missing:
                    return False
            else:
                return False

        elif key.startswith("item_") and key.endswith("_missing"):
            attr = key.removeprefix("item_").removesuffix("_missing")
            actual = get_item_attr(item, attr)
            missing = is_missing(actual)

            if expected_value is True and not missing:
                return False

            if expected_value is False and missing:
                return False

        elif key.startswith("item_") and key.endswith("_less_than"):
            attr = key.removeprefix("item_").removesuffix("_less_than")
            actual = get_item_attr(item, attr)

            if actual is None or not actual < expected_value:
                return False

        elif key.startswith("item_") and key.endswith("_greater_than"):
            attr = key.removeprefix("item_").removesuffix("_greater_than")
            actual = get_item_attr(item, attr)

            if actual is None or not actual > expected_value:
                return False

        elif key.startswith("item_") and key.endswith("_less_or_equal"):
            attr = key.removeprefix("item_").removesuffix("_less_or_equal")
            actual = get_item_attr(item, attr)

            if actual is None or not actual <= expected_value:
                return False

        elif key.startswith("item_") and key.endswith("_greater_or_equal"):
            attr = key.removeprefix("item_").removesuffix("_greater_or_equal")
            actual = get_item_attr(item, attr)

            if actual is None or not actual >= expected_value:
                return False

        elif key.startswith("item_") and key.endswith("_before"):
            attr = key.removeprefix("item_").removesuffix("_before")
            actual_date = parse_iso_date(get_item_attr(item, attr))
            expected_date = parse_iso_date(expected_value)

            if actual_date is None or expected_date is None or not actual_date < expected_date:
                return False

        elif key.startswith("item_") and key.endswith("_after"):
            attr = key.removeprefix("item_").removesuffix("_after")
            actual_date = parse_iso_date(get_item_attr(item, attr))
            expected_date = parse_iso_date(expected_value)

            if actual_date is None or expected_date is None or not actual_date > expected_date:
                return False

        elif key.startswith("item_") and key.endswith("_in"):
            attr = key.removeprefix("item_").removesuffix("_in")
            actual = get_item_attr(item, attr)

            if not isinstance(expected_value, list):
                return False

            if normalize_text(actual) not in [normalize_text(v) for v in expected_value]:
                return False

        elif key.startswith("item_") and key.endswith("_contains"):
            attr = key.removeprefix("item_").removesuffix("_contains")
            actual = get_item_attr(item, attr)

            if not contains(actual, expected_value):
                return False

        elif key.startswith("item_"):
            attr = key.removeprefix("item_")
            actual = get_item_attr(item, attr)

            if not equals(actual, expected_value):
                return False

        else:
            # Unknown condition: fail safely.
            return False

    return True


def build_full_evidence_summary(item: CandidateItem) -> Dict[str, Any]:
    return {
        "location": item.location,
        "category": item.category,
        "sameAs": item.sameAs,
        "declaredRiskIgnored": get_declared_item_risk(item),
        "accessibility": {
            "wheelchairAccessible": item.wheelchairAccessible,
            "stepFreeAccess": item.stepFreeAccess,
            "accessibleToilets": item.accessibleToilets
        },
        "sustainability": {
            "publicTransportAvailable": item.publicTransportAvailable,
            "visitorPressure": item.visitorPressure,
            "environmentalSensitivity": item.environmentalSensitivity,
            "localCommunityImpact": item.localCommunityImpact,
            "sustainabilityScore": item.sustainabilityScore
        },
        "provenance": {
            "dataSource": item.dataSource,
            "dataLicense": item.dataLicense,
            "lastUpdated": item.lastUpdated
        }
    }


def build_key_evidence(item: CandidateItem) -> Dict[str, Any]:
    return {
        "wheelchairAccessible": item.wheelchairAccessible,
        "stepFreeAccess": item.stepFreeAccess,
        "accessibleToilets": item.accessibleToilets,
        "sustainabilityScore": item.sustainabilityScore,
        "dataLicense": item.dataLicense,
        "lastUpdated": item.lastUpdated
    }


def evaluate_item(
    request: EthicsRequest,
    item: CandidateItem,
    policies: List[Dict[str, Any]]
) -> Dict[str, Any]:

    risks = []
    decisions = []
    rationale = []
    applied_policies = []
    required_actions = []
    triggered_rules = []

    declared_item_risk = get_declared_item_risk(item)

    if declared_item_risk:
        rationale.append(
            f"Declared risk value '{declared_item_risk}' was received but ignored for risk calculation."
        )

    for policy in policies:
        policy_id = policy.get("id", "unknown_policy")
        policy_description = policy.get("description", "")

        for rule in policy.get("rules", []):
            rule_id = rule.get("id", "unknown_rule")
            condition = rule.get("condition", {})

            if condition_matches(condition, request, item):
                risk = str(rule.get("riskImpact", "LOW")).upper()
                decision = str(rule.get("decision", "PROCEED")).upper()
                required_action = rule.get("requiredAction")

                risks.append(risk)
                decisions.append(decision)
                applied_policies.append(policy_id)
                triggered_rules.append(rule_id)

                rationale.append(
                    f"Rule '{rule_id}' from policy '{policy_id}' applied: {policy_description}"
                )

                if required_action:
                    required_actions.append(required_action)

    final_risk = max_risk(risks)
    final_decision = max_decision(decisions)

    if not triggered_rules:
        rationale.append("No policy rule was triggered for this item.")

    return {
        "id": item.id,
        "name": item.name,
        "riskLevel": final_risk,
        "decision": final_decision,
        "declaredRiskIgnored": declared_item_risk,
        "triggeredRules": unique_preserve_order(triggered_rules),
        "appliedPolicies": unique_preserve_order(applied_policies),
        "requiredActions": unique_preserve_order(required_actions),
        "rationale": unique_preserve_order(rationale),
        "keyEvidence": build_key_evidence(item),
        "evidenceSummary": build_full_evidence_summary(item)
    }


def make_summary(decision: str, risk_level: str, item_evaluations: List[Dict[str, Any]]) -> str:
    if not item_evaluations:
        return "No candidate item was evaluated."

    if decision == "PROCEED":
        return f"Recommendation can proceed. EaaS calculated {risk_level} risk from external policies."

    highest = max(item_evaluations, key=lambda item: decision_rank(item["decision"]))
    item_name = highest["name"]
    first_action = highest["requiredActions"][0] if highest["requiredActions"] else "Review the recommendation before presenting it to the user."

    return f"{item_name} requires {decision}. Main reason: {first_action}"


def build_case_analysis(request: EthicsRequest, overall_risk: str) -> Dict[str, Any]:
    return {
        "description": "Detailed case analysis stored for audit purposes.",
        "operation": request.operation,
        "numberOfCandidateItems": len(request.candidateItems),
        "declaredRiskIgnored": request.declaredRisk,
        "declaredItemRisksIgnored": [
            {
                "id": item.id,
                "name": item.name,
                "declaredRisk": get_declared_item_risk(item)
            }
            for item in request.candidateItems
            if get_declared_item_risk(item) is not None
        ],
        "detectedRisk": overall_risk,
        "itemsAnalyzed": [
            {
                "id": item.id,
                "name": item.name,
                "location": item.location,
                "category": item.category,
                "evidenceUsed": build_full_evidence_summary(item)
            }
            for item in request.candidateItems
        ]
    }


def model_to_dict(model: BaseModel) -> Dict[str, Any]:
    if hasattr(model, "model_dump"):
        return model.model_dump()
    return model.dict()


def store_audit_record(record: Dict[str, Any]) -> str:
    with open(AUDIT_FILE, "a", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False) + "\n")

    return f"/audit/{record['evaluationId']}"


def find_audit_record(evaluation_id: str) -> Optional[Dict[str, Any]]:
    if not AUDIT_FILE.exists():
        return None

    with open(AUDIT_FILE, "r", encoding="utf-8") as f:
        for line in f:
            record = json.loads(line)
            if record.get("evaluationId") == evaluation_id:
                return record

    return None


# -----------------------------
# API endpoints
# -----------------------------

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "service": "EaaS",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }


@app.get("/policies")
def list_policies():
    policies = load_policies()
    return {
        "count": len(policies),
        "policies": [
            {
                "id": policy.get("id"),
                "description": policy.get("description"),
                "domain": policy.get("domain")
            }
            for policy in policies
        ]
    }


@app.post("/ethics/evaluate", response_model=EthicsResponse)
def evaluate_ethics(request: EthicsRequest):
    policies = load_policies()

    item_evaluations = [
        evaluate_item(request, item, policies)
        for item in request.candidateItems
    ]

    overall_risk = max_risk([item["riskLevel"] for item in item_evaluations])
    overall_decision = max_decision([item["decision"] for item in item_evaluations])

    applied_policies = unique_preserve_order([
        policy
        for item in item_evaluations
        for policy in item["appliedPolicies"]
    ])

    required_actions = unique_preserve_order([
        action
        for item in item_evaluations
        for action in item["requiredActions"]
    ])

    evaluation_id = str(uuid.uuid4())
    timestamp = datetime.now(timezone.utc).isoformat()

    compact_items = [
        CompactItemEvaluation(
            id=item["id"],
            name=item["name"],
            riskLevel=item["riskLevel"],
            decision=item["decision"],
            declaredRiskIgnored=item["declaredRiskIgnored"],
            triggeredRules=item["triggeredRules"],
            keyEvidence=item["keyEvidence"]
        )
        for item in item_evaluations
    ]

    compact_response = EthicsResponse(
        evaluationId=evaluation_id,
        timestamp=timestamp,
        operation=request.operation,
        riskLevel=overall_risk,
        decision=overall_decision,
        summary=make_summary(overall_decision, overall_risk, item_evaluations),
        declaredRiskIgnored={
            "topLevel": request.declaredRisk,
            "items": [
                {
                    "id": item.id,
                    "name": item.name,
                    "value": get_declared_item_risk(item)
                }
                for item in request.candidateItems
                if get_declared_item_risk(item) is not None
            ]
        },
        appliedPolicies=applied_policies,
        requiredActions=required_actions,
        items=compact_items,
        auditRecord=f"/audit/{evaluation_id}"
    )

    audit_record = {
        "evaluationId": evaluation_id,
        "timestamp": timestamp,
        "operation": request.operation,
        "riskLevel": overall_risk,
        "decision": overall_decision,
        "summary": compact_response.summary,
        "caseAnalysis": build_case_analysis(request, overall_risk),
        "governanceDecision": {
            "description": "Governance decision defines what the system should do after ethical analysis.",
            "decision": overall_decision,
            "allowedDecisions": ["PROCEED", "REVISE", "ESCALATE", "REJECT"],
            "requiredActions": required_actions
        },
        "rationale": [
            f"Risk was calculated internally from {len(policies)} external policies.",
            "Declared risk values sent by DaaS or caller were ignored.",
            f"The highest detected risk level was {overall_risk}.",
            f"The final governance decision was {overall_decision}."
        ],
        "appliedPolicies": applied_policies,
        "requiredActions": required_actions,
        "itemEvaluationsDetailed": item_evaluations,
        "compactResponse": model_to_dict(compact_response),
        "auditRecord": f"/audit/{evaluation_id}"
    }

    store_audit_record(audit_record)

    return compact_response


@app.get("/audit/{evaluation_id}")
def get_audit_record(evaluation_id: str):
    record = find_audit_record(evaluation_id)

    if not record:
        raise HTTPException(status_code=404, detail="Audit record not found.")

    return record
