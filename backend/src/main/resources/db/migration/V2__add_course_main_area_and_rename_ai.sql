ALTER TABLE course ADD COLUMN main_area VARCHAR(100);

UPDATE interest_area
SET name = 'AI'
WHERE name = 'AI_INFORMATION_SERVICE';
