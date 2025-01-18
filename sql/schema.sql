-- Step 1: Create the Database
CREATE DATABASE IF NOT EXISTS projecttopicmanagement;

-- Step 2: Use the Database
USE projecttopicmanagement;

-- Step 3: Create the Projects Table
CREATE TABLE IF NOT EXISTS Projects (
    project_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    project_name TEXT NOT NULL,
    remaining_team_members INTEGER NOT NULL,
    supervisor_name TEXT NOT NULL
);

-- Step 4: Drop the Students table if it exists and recreate it
DROP TABLE IF EXISTS Students;

-- Step 5: Create the Students Table
CREATE TABLE IF NOT EXISTS Students (
    student_id VARCHAR(10) PRIMARY KEY,
    student_name TEXT NOT NULL,
    major TEXT NOT NULL,
    selected_project_id INTEGER,
    FOREIGN KEY (selected_project_id) REFERENCES Projects(project_id)
);

-- Step 6: Insert Sample Data into the Projects Table
INSERT INTO Projects (project_name, remaining_team_members, supervisor_name)
VALUES 
('Personal Finance Assistant System', 3, 'Zhang'),
('Office Supply Management System', 4, 'Li');

-- Step 7: Insert Sample Data into the Students Table
INSERT INTO Students (student_id, student_name, major)
VALUES 
('001', 'Wang', 'Computer Science'),
('002', 'Zhao', 'Information Systems');
