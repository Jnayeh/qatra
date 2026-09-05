CREATE SEQUENCE demand_forecast_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE demand_forecasts (
    id BIGINT NOT NULL,
    blood_type VARCHAR(255) NOT NULL CHECK (blood_type IN ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN')),
    center_id BIGINT,
    city VARCHAR(255),
    country VARCHAR(255),
    forecast_period_type VARCHAR(255) NOT NULL CHECK (forecast_period_type IN ('WEEK','MONTH')),
    week_number INTEGER,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    predicted_demand DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION,
    generated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (center_id) REFERENCES donation_centers(id)
);

CREATE INDEX idx_demand_forecasts_blood_type ON demand_forecasts(blood_type);
CREATE INDEX idx_demand_forecasts_center ON demand_forecasts(center_id);
CREATE INDEX idx_demand_forecasts_year_month ON demand_forecasts(year, month);
CREATE INDEX idx_demand_forecasts_city ON demand_forecasts(city);
CREATE INDEX idx_demand_forecasts_country ON demand_forecasts(country);
