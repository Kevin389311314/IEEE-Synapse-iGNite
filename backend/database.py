import os
import datetime
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./phishlens_backend.db")

engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False} if "sqlite" in DATABASE_URL else {}
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class ScanHistory(Base):
    __tablename__ = "scan_history"

    id = Column(Integer, primary_key=True, index=True)
    scan_type = Column(String(32), nullable=False)  # TEXT, URL, SCREENSHOT
    raw_input = Column(Text, nullable=False)
    title = Column(String(256), nullable=False)
    risk_score = Column(Integer, nullable=False)
    risk_level = Column(String(32), nullable=False)
    explanation = Column(Text, nullable=False)
    indicators_json = Column(Text, default="[]")
    safety_actions_json = Column(Text, default="[]")
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

def init_db():
    Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
