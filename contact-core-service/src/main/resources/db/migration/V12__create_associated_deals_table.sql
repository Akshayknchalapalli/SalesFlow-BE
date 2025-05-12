-- Create associated_deals table
CREATE TABLE associated_deals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    deal_id UUID NOT NULL,
    deal_name VARCHAR(255) NOT NULL,
    deal_amount DECIMAL(15,2),
    deal_stage VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_associated_deals_contact_id ON associated_deals(contact_id);
CREATE INDEX idx_associated_deals_deal_id ON associated_deals(deal_id); 