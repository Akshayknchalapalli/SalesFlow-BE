-- Update platform column in contact_social_profiles table to use VARCHAR
ALTER TABLE contact_social_profiles 
ALTER COLUMN platform TYPE VARCHAR(50) USING platform::VARCHAR(50); 