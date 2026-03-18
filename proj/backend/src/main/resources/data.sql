DROP TABLE IF EXISTS documents;
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(768) -- 768 matches Gemini 2.0 Flash/Pro
);

CREATE INDEX ON documents USING hnsw (embedding vector_cosine_ops);
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_entity ON documents
(
    (metadata->>'type'), 
    (metadata->>'id')
);

DROP TABLE IF EXISTS spring_ai_chat_memory;

CREATE TABLE spring_ai_chat_memory (
    conversation_id VARCHAR(255) NOT NULL,
    content TEXT,          -- Library looks for 'content', NOT 'message_content'
    type VARCHAR(255),     -- Library looks for 'type', NOT 'message_type'
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Library looks for 'timestamp', NOT 'created_at'
    metadata TEXT,
    PRIMARY KEY (conversation_id, timestamp)
);

-- Insert segments only if they don't exist
INSERT INTO segments (name) VALUES
('Portuguese People'),
('Hebrews'),
('Americans'),
('Asians'),
('Latins');

-- Insert campaigns only if they don't exist
INSERT INTO campaign (name, description, created_at, due_date, status) VALUES
('Summer Sale', 'Discounts on summer clothing', '2024-06-01', '2024-06-30', 'ACTIVE'),
('Winter Clearance', 'Clearance sale for winter items', '2024-01-15', '2024-02-15', 'FINISHED'),
('Back to School', 'Promotions on school supplies', '2024-08-01', '2024-09-01', 'IN_PROGRESS'),
('Holiday Specials', 'Special offers for the holiday season', '2024-11-20', '2024-12-31', 'IN_PROGRESS'),
('Spring Festival', 'Celebrate spring with discounts', '2024-03-10', '2024-04-10', 'FINISHED');

-- Insert campaign_segments only if they don't exist
INSERT INTO campaign_segments (campaign_id, segment_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5)
ON CONFLICT (campaign_id, segment_id) DO NOTHING;

-- Insert workflow only if it doesn't exist
INSERT INTO workflow (name, description, created_at, modified_at, is_ready_to_use)
VALUES 
('Summer Sale Automation', 'Workflow for summer campaign automation', NOW(), NULL, false);

-- Update campaign workflow only if not already set
UPDATE campaign SET workflow_id = 1 WHERE id = 1;

INSERT INTO email_template (name, subject, body) VALUES
('Welcome Email', 'Welcome to Our Service', '<h1>Welcome!</h1><p>Thank you for joining us.</p>'),
('Promotion Email', 'Exclusive Offer Just for You', '<h1>Special Promotion</h1><p>Enjoy a 20% discount on your next purchase.</p>'),
('Newsletter', 'Monthly Updates from Our Team', '<h1>Our Latest News</h1><p>Stay informed with our monthly newsletter.</p>');

-- Insert Email Templates into campaign_materials table
INSERT INTO campaign_materials (name, dtype, subject, body) VALUES
('Welcome Email', 'EMAIL', 'Welcome to Our Service', '<h1>Welcome!</h1><p>Thank you for joining us.</p>'),
('Promotion Email', 'EMAIL', 'Exclusive Offer Just for You', '<h1>Special Promotion</h1><p>Enjoy a 20% discount on your next purchase.</p>'),
('Newsletter', 'EMAIL', 'Monthly Updates from Our Team', '<h1>Our Latest News</h1><p>Stay informed with our monthly newsletter.</p>');

-- Insert Form Templates into campaign_materials table  
INSERT INTO campaign_materials (name, dtype, description, fields) VALUES
('Contact Us Form', 'FORM', 'A form for users to contact us', '[{"label": "Name", "type": "text"}, {"label": "Email", "type": "email"}, {"label": "Message", "type": "textarea"}]'),
('Feedback Form', 'FORM', 'A form to collect user feedback', '[{"label": "Rating", "type": "number"}, {"label": "Comments", "type": "textarea"}]');

-- Insert Landing Pages into campaign_materials table
INSERT INTO campaign_materials (name, dtype, body) VALUES
('Home Page', 'LANDING_PAGE', '<h1>Welcome to Our Home Page</h1><p>Discover our services and offerings.</p>'),
('Product Page', 'LANDING_PAGE', '<h1>Our Products</h1><p>Explore our range of products.</p>');