ALTER TABLE course_prerequisite DROP CONSTRAINT chk_course_prerequisite_relation_type;

ALTER TABLE course_prerequisite
    ADD CONSTRAINT chk_course_prerequisite_relation_type
    CHECK (relation_type IN ('PREREQUISITE', 'RECOMMENDED', 'INCOMPATIBLE'));
