-- V2__add_profile_completed.sql
-- Add profile_completed column to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_completed BOOLEAN DEFAULT FALSE;

-- Update existing users to have profile_completed = TRUE if they have all required fields
UPDATE users SET profile_completed = TRUE 
WHERE full_name IS NOT NULL 
  AND email IS NOT NULL 
  AND (student_number IS NOT NULL OR role != 'student');