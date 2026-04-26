-- =====================================================
-- Migration V5: Add file_name column to submissions table
-- =====================================================

-- Add file_name column
ALTER TABLE submissions ADD COLUMN file_name VARCHAR(255);

-- Add file_size column
ALTER TABLE submissions ADD COLUMN file_size BIGINT;

-- Create index
CREATE INDEX idx_submissions_file_name ON submissions(file_name);