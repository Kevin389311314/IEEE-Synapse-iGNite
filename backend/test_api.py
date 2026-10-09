import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert "version" in data

def test_analyze_urgent_bank_smish():
    payload = {
        "text": "URGENT: Wells Fargo alert. Your account is suspended. Verify credentials immediately within 24 hours at http://wellsfargo-update.tk/login"
    }
    response = client.post("/api/v1/analyze/text", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_score"] >= 60
    assert data["risk_level"] == "HIGH"
    assert len(data["indicators"]) >= 2
    assert len(data["safety_actions"]) > 0

def test_analyze_benign_message():
    payload = {
        "text": "Your doctor appointment is confirmed for Tuesday at 3:00 PM. Reply STOP to cancel."
    }
    response = client.post("/api/v1/analyze/text", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_score"] < 30
    assert data["risk_level"] == "LOW"

def test_analyze_raw_ip_url():
    payload = {
        "url": "http://192.168.1.100/secure/login"
    }
    response = client.post("/api/v1/analyze/url", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_score"] >= 40
    assert any(i["id"] == "URL_IP_HOST" for i in data["indicators"])

def test_analyze_punycode_url():
    payload = {
        "url": "https://xn--pple-43d.com/signin"
    }
    response = client.post("/api/v1/analyze/url", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert any(i["id"] == "URL_PUNYCODE" for i in data["indicators"])

def test_empty_input_validation():
    response = client.post("/api/v1/analyze/text", json={"text": "   "})
    assert response.status_code == 400

    response_url = client.post("/api/v1/analyze/url", json={"url": ""})
    assert response_url.status_code == 400

def test_history_logging():
    client.post("/api/v1/analyze/text", json={"text": "Security test for history logging."})
    history_resp = client.get("/api/v1/history")
    assert history_resp.status_code == 200
    records = history_resp.json()
    assert len(records) > 0
