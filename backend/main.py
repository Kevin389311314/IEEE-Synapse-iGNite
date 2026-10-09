import os
import json
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List

from database import init_db, get_db, ScanHistory
from models import (
    TextAnalyzeRequest,
    UrlAnalyzeRequest,
    ScreenshotAnalyzeRequest,
    AnalyzeResponse,
    HealthResponse,
    HistoryRecordModel
)
from analyzer import analyze_text_heuristics, analyze_url_heuristics

app = FastAPI(
    title="PhishLens API – Cybersecurity Phishing & Scam Detector",
    description="FastAPI service for transparent heuristic phishing analysis and risk scoring.",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def on_startup():
    init_db()

@app.get("/health", response_model=HealthResponse)
def health_check():
    llm_key = os.getenv("GEMINI_API_KEY") or os.getenv("OPENAI_API_KEY")
    return HealthResponse(
        status="ok",
        version="1.0.0",
        llm_enabled=bool(llm_key),
        database_connected=True
    )

@app.post("/api/v1/analyze/text", response_model=AnalyzeResponse)
def analyze_text_endpoint(
    req: TextAnalyzeRequest,
    db: Session = Depends(get_db)
):
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Text field cannot be empty.")

    result = analyze_text_heuristics(req.text)

    # Persist in SQLite
    try:
        record = ScanHistory(
            scan_type="TEXT",
            raw_input=req.text[:2000],
            title=result.title,
            risk_score=result.risk_score,
            risk_level=result.risk_level,
            explanation=result.explanation,
            indicators_json=json.dumps([i.dict() for i in result.indicators]),
            safety_actions_json=json.dumps(result.safety_actions)
        )
        db.add(record)
        db.commit()
    except Exception as e:
        print(f"Warning: could not persist history: {e}")

    return result

@app.post("/api/v1/analyze/url", response_model=AnalyzeResponse)
def analyze_url_endpoint(
    req: UrlAnalyzeRequest,
    db: Session = Depends(get_db)
):
    if not req.url.strip():
        raise HTTPException(status_code=400, detail="URL field cannot be empty.")

    result = analyze_url_heuristics(req.url)

    # Persist in SQLite
    try:
        record = ScanHistory(
            scan_type="URL",
            raw_input=req.url[:1000],
            title=result.title,
            risk_score=result.risk_score,
            risk_level=result.risk_level,
            explanation=result.explanation,
            indicators_json=json.dumps([i.dict() for i in result.indicators]),
            safety_actions_json=json.dumps(result.safety_actions)
        )
        db.add(record)
        db.commit()
    except Exception as e:
        print(f"Warning: could not persist history: {e}")

    return result

@app.post("/api/v1/analyze/screenshot", response_model=AnalyzeResponse)
def analyze_screenshot_endpoint(
    req: ScreenshotAnalyzeRequest,
    db: Session = Depends(get_db)
):
    if not req.extracted_text.strip():
        raise HTTPException(status_code=400, detail="Extracted text cannot be empty.")

    result = analyze_text_heuristics(req.extracted_text)
    result.title = f"Screenshot: {result.title}"

    try:
        record = ScanHistory(
            scan_type="SCREENSHOT",
            raw_input=req.extracted_text[:2000],
            title=result.title,
            risk_score=result.risk_score,
            risk_level=result.risk_level,
            explanation=result.explanation,
            indicators_json=json.dumps([i.dict() for i in result.indicators]),
            safety_actions_json=json.dumps(result.safety_actions)
        )
        db.add(record)
        db.commit()
    except Exception as e:
        print(f"Warning: could not persist history: {e}")

    return result

@app.get("/api/v1/history", response_model=List[HistoryRecordModel])
def get_history_endpoint(
    limit: int = 50,
    db: Session = Depends(get_db)
):
    records = db.query(ScanHistory).order_by(ScanHistory.id.desc()).limit(limit).all()
    return [
        HistoryRecordModel(
            id=r.id,
            type=r.scan_type,
            raw_input=r.raw_input,
            risk_score=r.risk_score,
            risk_level=r.risk_level,
            title=r.title,
            explanation=r.explanation,
            created_at=str(r.created_at)
        )
        for r in records
    ]

@app.delete("/api/v1/history/{record_id}")
def delete_history_endpoint(
    record_id: int,
    db: Session = Depends(get_db)
):
    rec = db.query(ScanHistory).filter(ScanHistory.id == record_id).first()
    if not rec:
        raise HTTPException(status_code=404, detail="Record not found")
    db.delete(rec)
    db.commit()
    return {"message": "Record deleted successfully"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
