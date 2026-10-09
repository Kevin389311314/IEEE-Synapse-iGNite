from pydantic import BaseModel, Field
from typing import List, Optional, Dict

class IndicatorModel(BaseModel):
    id: str
    rule_name: str
    category: str
    severity: str  # LOW, MEDIUM, HIGH
    description: str
    evidence: str
    points: int

class TextAnalyzeRequest(BaseModel):
    text: str = Field(..., description="Message, email body, or SMS content to scan")
    include_llm: bool = Field(False, description="Whether to query optional LLM API")

class UrlAnalyzeRequest(BaseModel):
    url: str = Field(..., description="URL to analyze without visiting")
    include_llm: bool = Field(False, description="Whether to query optional LLM API")

class ScreenshotAnalyzeRequest(BaseModel):
    extracted_text: str = Field(..., description="OCR text extracted from screenshot")
    image_base64: Optional[str] = Field(None, description="Optional base64 encoded image")
    include_llm: bool = False

class AnalyzeResponse(BaseModel):
    risk_score: int
    risk_level: str  # LOW, MEDIUM, HIGH
    title: str
    explanation: str
    indicators: List[IndicatorModel]
    safety_actions: List[str]
    engine: str = "PhishLens Rule-Based Engine"

class HealthResponse(BaseModel):
    status: str
    version: str
    llm_enabled: bool
    database_connected: bool

class HistoryRecordModel(BaseModel):
    id: int
    type: str
    raw_input: str
    risk_score: int
    risk_level: str
    title: str
    explanation: str
    created_at: str
