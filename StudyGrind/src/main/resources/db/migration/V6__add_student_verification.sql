-- V6__add_student_verification.sql
-- Add student verification columns to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS student_card_path VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS student_card_file_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_status VARCHAR(50) DEFAULT 'pending';
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_notes TEXT;

-- Add index for faster queries on verification status
CREATE INDEX IF NOT EXISTS idx_users_verification_status ON users(verification_status);

-- Update existing students to verified status (for backward compatibility)
UPDATE users SET verification_status = 'approved' WHERE role = 'student' AND (verification_status IS NULL OR verification_status = 'pending');

-- Add constraint to ensure verification_status has valid values
ALTER TABLE users ADD CONSTRAINT chk_verification_status CHECK (verification_status IN ('pending', 'approved', 'rejected'));