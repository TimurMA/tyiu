CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    roles TEXT[] NOT NULL,
    first_name TEXT,
    last_name TEXT
);
CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    roles TEXT[] NOT NULL
);
CREATE TABLE IF NOT EXISTS idea (
    id BIGSERIAL PRIMARY KEY,
    initiator_id BIGINT REFERENCES users (id),
    name TEXT,
    group_expert_id BIGINT REFERENCES groups (id),
    group_project_office_id BIGINT REFERENCES groups (id),
    status TEXT,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    project_type TEXT,
    problem TEXT,
    solution TEXT,
    result TEXT,
    customer TEXT,
    contact_person TEXT,
    description TEXT,
    suitability BIGINT,
    budget BIGINT,
    technical_realizability BIGINT,
    pre_assessment REAL,
    rating REAL
);
CREATE TABLE IF NOT EXISTS temporary (
    id BIGSERIAL PRIMARY KEY,
    url TEXT,
    date_expired TIMESTAMP,
    email TEXT,
    new_email TEXT,
    old_email TEXT,
    code INT,
    roles TEXT[]
);
CREATE TABLE IF NOT EXISTS group_user (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users (id),
    group_id BIGINT REFERENCES groups (id)
);
CREATE TABLE IF NOT EXISTS company (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS company_user (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users (id),
    company_id BIGINT REFERENCES company (id)
);
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    senderEmail TEXT NOT NULL,
    createdAt TIMESTAMP,
    idea_id BIGINT REFERENCES idea (id)
);
CREATE TABLE IF NOT EXISTS skill (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    confirmed BOOLEAN NOT NULL,
    creator_id BIGINT,
    updater_id BIGINT,
    deleter_id BIGINT
);
CREATE TABLE IF NOT EXISTS rating (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT REFERENCES idea (id),
    expert_id BIGINT REFERENCES users (id),
    market_value BIGINT,
    originality BIGINT,
    suitability INT,
    budget BIGINT,
    technical_realizability BIGINT,
    confirmed BOOLEAN,
    rating REAL
);
CREATE TABLE IF NOT EXISTS team (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    closed BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    owner_id BIGINT REFERENCES users (id),
    leader_id BIGINT REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS team_invitation (
    id BIGSERIAL PRIMARY KEY,
    team_name TEXT NOT NULL,
    team_id BIGINT REFERENCES team (id),
    receiver_id BIGINT REFERENCES users (id),
    created_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS team_member (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT REFERENCES team (id),
    member_id BIGINT REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS team_skill (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT REFERENCES team (id),
    skill_id BIGINT REFERENCES skill (id)
);
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    team_id BIGINT REFERENCES team (id)
);
CREATE TABLE IF NOT EXISTS project_invitation (
    id BIGSERIAL PRIMARY KEY,
    project_name TEXT,
    project_id BIGINT REFERENCES project (id),
    receiver_id BIGINT REFERENCES users (id),
    created_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS project_request (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES project (id),
    user_id BIGINT REFERENCES users (id),
    email TEXT,
    first_name TEXT,
    last_name TEXT,
    created_at TIMESTAMP
);