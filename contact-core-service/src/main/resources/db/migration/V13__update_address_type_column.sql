-- Update type column in contact_addresses table to use VARCHAR
ALTER TABLE contact_addresses 
ALTER COLUMN type TYPE VARCHAR(50) USING type::VARCHAR(50); 