
SET autocommit=0;
SET foreign_key_checks=0;
SET unique_checks=0;
CREATE DATABASE IF NOT EXISTS ielts_db;
USE ielts_db;

-- 1. Disable foreign key checks for clean drop
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Drop ALL Listening Tables (Clean Slate)
DROP TABLE IF EXISTS listening_user_answer;
DROP TABLE IF EXISTS listening_question;
DROP TABLE IF EXISTS listening_question_group;
DROP TABLE IF EXISTS listening_section;
DROP TABLE IF EXISTS listening_question_type;
DROP TABLE IF EXISTS listening_test;

-- 3. Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

--
## 📝 Table Creation for LISTENING

-- 1️⃣ Table: listening_test (Parent)
CREATE TABLE listening_test (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    test_name VARCHAR(255) NOT NULL,
    test_level ENUM('Academic', 'General') DEFAULT 'Academic',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2️⃣ Table: listening_question_type (CONSOLIDATED)
CREATE TABLE listening_question_type (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL
);

INSERT INTO listening_question_type (type_name) VALUES
('Multiple Choice (Single Answer)'), -- type_id 1
('Multiple Choice (Multiple Answers)'), -- type_id 2
('Matching'), -- type_id 3
('Completion/Labelling (Visual & Textual Gaps)'), -- type_id 4
('Sentence Completion'), -- type_id 5
('Short Answer Questions'); -- type_id 6

-- 3️⃣ Table: listening_section
CREATE TABLE listening_section (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    test_id INT NOT NULL,
    section_name VARCHAR(255),
    intro_text TEXT,
    audio_url VARCHAR(500) NOT NULL,
    transcript TEXT,
    section_order INT DEFAULT 1,
    FOREIGN KEY (test_id) REFERENCES listening_test(test_id) ON DELETE CASCADE
);

-- 4️⃣ Table: listening_question_group (CORRECTED FOREIGN KEY)
CREATE TABLE listening_question_group (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    instructions TEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    group_order INT DEFAULT 1,
    -- CORRECTED LINE BELOW: It must reference listening_section(section_id)
    FOREIGN KEY (section_id) REFERENCES listening_section(section_id) ON DELETE CASCADE
    -- The original script had: FOREIGN KEY (section_id) REFERENCES listening_test(test_id)
);

-- 5️⃣ Table: listening_question
CREATE TABLE listening_question (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    type_id INT NOT NULL,
    question_text TEXT NOT NULL,
    question_image_url VARCHAR(500) NULL, -- If a specific question has an image option (e.g., A, B, C options are images)
    options JSON NULL,
    correct_answer VARCHAR(255),
    question_order INT DEFAULT 1,
    FOREIGN KEY (group_id) REFERENCES listening_question_group(group_id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES listening_question_type(type_id)
);

-- 6️⃣ Table: listening_user_answer
CREATE TABLE listening_user_answer (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    question_id INT NOT NULL,
    user_response VARCHAR(255),
    is_correct BOOLEAN,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES listening_question(question_id) ON DELETE CASCADE
);

--
## 🚀 Data Insertion: Cambridge 20 - Test 1

-- Set up variables for easier linking
SET @TYPE_MC_SINGLE = 1;
SET @TYPE_MC_MULTI = 2;
SET @TYPE_COMPLETION = 4; -- Consolidated Type

-- 1️⃣ Insert Test
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 20 - Test 1', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id, 'Part 1: Restaurant Booking', 'Questions 1–10. Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T1S1.m4a', '...[Part 1 Transcript]...', 1),
(@test_id, 'Part 2: Edelman Pottery', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T1S2.m4a', '...[Part 2 Transcript]...', 2),
(@test_id, 'Part 3: Loneliness Research', 'Questions 21–30.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T1S3.m4a', '...[Part 3 Transcript]...', 3),
(@test_id, 'Part 4: The Role of Rivers in Cities', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T1S4.m4a', '...[Part 4 Transcript]...', 4);

SET @s1_id = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id);
SET @s2_id = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id);
SET @s3_id = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id);
SET @s4_id = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Table/Notes Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id, 'Complete the notes below about the three restaurants. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam-20-part1.png', 1);
SET @g1_id = LAST_INSERT_ID();

-- G2: Part 2 - MC Single (Q11-16)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id, 'Questions 11-16. Choose the correct letter A, B or C.', NULL, 1);
SET @g2_1_id = LAST_INSERT_ID();

-- G3: Part 2 - MC Multi (Q17-18)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id, 'Questions 17 and 18. Choose TWO letters, A–E. Which TWO things does Heather explain about kilns?', NULL, 2);
SET @g2_2_id = LAST_INSERT_ID();

-- G4: Part 2 - MC Multi (Q19-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id, 'Questions 19 and 20. Choose TWO letters, A–E. Which TWO points does Heather make about a potter’s tools?', NULL, 3);
SET @g2_3_id = LAST_INSERT_ID();

-- G5: Part 3 - MC Multi (Q21-22)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id, 'Questions 21 and 22. Choose TWO letters, A–E. Which TWO things do the students both believe are responsible for the increase in loneliness?', NULL, 1);
SET @g3_1_id = LAST_INSERT_ID();

-- G6: Part 3 - MC Multi (Q23-24)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id, 'Questions 23 and 24. Choose TWO letters, A–E. Which TWO health risks associated with loneliness do the students agree are based on solid evidence?', NULL, 2);
SET @g3_2_id = LAST_INSERT_ID();

-- G7: Part 3 - MC Multi (Q25-26)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id, 'Questions 25 and 26. Choose TWO letters, A–E. Which TWO opinions do both the students express about the evolutionary theory of loneliness?', NULL, 3);
SET @g3_3_id = LAST_INSERT_ID();

-- G8: Part 3 - MC Single (Q27-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id, 'Questions 27-30. Choose the correct letter A, B or C.', NULL, 4);
SET @g3_4_id = LAST_INSERT_ID();

-- G9: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id, 'Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Screenshot%202025-11-23%20062753.png', 1);
SET @g4_id = LAST_INSERT_ID();

-- 4️⃣ Insert Individual Questions

-- PART 1 (Q1-10) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_id, @TYPE_COMPLETION, 'The Junction: Food specialty: 1. $\underline{\hspace{2em}}$', 'fish', 1),
(@g1_id, @TYPE_COMPLETION, 'The Junction: Great atmosphere, and before dinner, guests can have a drink on the 2. $\underline{\hspace{2em}}$', 'roof', 2),
(@g1_id, @TYPE_COMPLETION, 'Paloma: All the food served is 3. $\underline{\hspace{2em}}$', 'Spanish', 3),
(@g1_id, @TYPE_COMPLETION, 'Paloma: Limited choice of 4. $\underline{\hspace{2em}}$ dishes', 'vegetarian', 4),
(@g1_id, @TYPE_COMPLETION, 'The 5. $\underline{\hspace{2em}}$', 'Audley', 5),
(@g1_id, @TYPE_COMPLETION, 'The Audley: Location: in a 6. $\underline{\hspace{2em}}$ near Baxter Bridge', 'hotel', 6),
(@g1_id, @TYPE_COMPLETION, 'The Audley: Highly-rated by newspaper 7. $\underline{\hspace{2em}}$', 'reviews', 7),
(@g1_id, @TYPE_COMPLETION, 'The Audley: Chef only uses 8. $\underline{\hspace{2em}}$ products', 'local', 8),
(@g1_id, @TYPE_COMPLETION, 'The Audley: Set lunch costs £ 9. $\underline{\hspace{2em}}$ per person', '30', 9),
(@g1_id, @TYPE_COMPLETION, 'The Audley: Portion sizes are considered 10. $\underline{\hspace{2em}}$', 'average', 10);

-- PART 2 (Q11-16) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_id, @TYPE_MC_SINGLE, 'Heather says pottery differs from other art forms because', '{"A": "it lasts longer in the ground.", "B": "it is practised by more people.", "C": "it can be repaired more easily."}', 'A', 11),
(@g2_1_id, @TYPE_MC_SINGLE, 'Archaeologists sometimes identify the use of ancient pottery from', '{"A": "the clay it was made with.", "B": "the marks that are on it.", "C": "the basic shape of it."}', 'B', 12),
(@g2_1_id, @TYPE_MC_SINGLE, 'Some people join Heather\'s pottery class because they want to', '{"A": "create an item that looks very old.", "B": "find something that they are good at.", "C": "make something that will outlive them."}', 'C', 13),
(@g2_1_id, @TYPE_MC_SINGLE, 'What does Heather value most about being a potter?', '{"A": "its calming effect", "B": "its messy nature", "C": "its physical benefits"}', 'A', 14),
(@g2_1_id, @TYPE_MC_SINGLE, 'Most of the visitors to Edelman Pottery', '{"A": "bring friends to join courses.", "B": "have never made a pot before.", "C": "try to learn techniques too quickly."}', 'B', 15),
(@g2_1_id, @TYPE_MC_SINGLE, 'Heather reminds her visitors that they should', '{"A": "put on their aprons.", "B": "change their clothes.", "C": "take off their jewellery"}', 'C', 16);

-- PART 2 (Q17-20) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_2_id, @TYPE_MC_MULTI, 'Which TWO things does Heather explain about kilns?', '{"A": "what their function is", "B": "when they were invented", "C": "ways of keeping them safe", "D": "where to put one in your home", "E": "what some people use instead of one"}', 'A,E', 17),
(@g2_2_id, @TYPE_MC_MULTI, 'Which TWO things does Heather explain about kilns? (Second Answer)', '{"A": "what their function is", "B": "when they were invented", "C": "ways of keeping them safe", "D": "where to put one in your home", "E": "what some people use instead of one"}', 'A,E', 18),
(@g2_3_id, @TYPE_MC_MULTI, 'Which TWO points does Heather make about a potter''s tools?', '{"A": "Some are hard to hold.", "B": "Some are worth buying.", "C": "Some are essential items.", "D": "Some have memorable names.", "E": "Some are available for use by participants."}', 'C,E', 19),
(@g2_3_id, @TYPE_MC_MULTI, 'Which TWO points does Heather make about a potter''s tools? (Second Answer)', '{"A": "Some are hard to hold.", "B": "Some are worth buying.", "C": "Some are essential items.", "D": "Some have memorable names.", "E": "Some are available for use by participants."}', 'C,E', 20);

-- PART 3 (Q21-26) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_id, @TYPE_MC_MULTI, 'Which TWO things do the students both believe are responsible for the increase in loneliness?', '{"A": "social media", "B": "smaller nuclear families", "C": "urban design", "D": "longer lifespans", "E": "a mobile workforce"}', 'C,E', 21),
(@g3_1_id, @TYPE_MC_MULTI, 'Which TWO things do the students both believe are responsible for the increase in loneliness? (Second Answer)', '{"A": "social media", "B": "smaller nuclear families", "C": "urban design", "D": "longer lifespans", "E": "a mobile workforce"}', 'C,E', 22),
(@g3_2_id, @TYPE_MC_MULTI, 'Which TWO health risks associated with loneliness do the students agree are based on solid evidence?', '{"A": "a weakened immune system", "B": "dementia", "C": "cancer", "D": "obesity", "E": "cardiovascular disease"}', 'A,C', 23),
(@g3_2_id, @TYPE_MC_MULTI, 'Which TWO health risks associated with loneliness do the students agree are based on solid evidence? (Second Answer)', '{"A": "a weakened immune system", "B": "dementia", "C": "cancer", "D": "obesity", "E": "cardiovascular disease"}', 'A,C', 24),
(@g3_3_id, @TYPE_MC_MULTI, 'Which TWO opinions do both the students express about the evolutionary theory of loneliness?', '{"A": "It has little practical relevance.", "B": "It needs further investigation.", "C": "It is misleading.", "D": "It should be more widely accepted.", "E": "It is difficult to understand."}', 'A,B', 25),
(@g3_3_id, @TYPE_MC_MULTI, 'Which TWO opinions do both the students express about the evolutionary theory of loneliness? (Second Answer)', '{"A": "It has little practical relevance.", "B": "It needs further investigation.", "C": "It is misleading.", "D": "It should be more widely accepted.", "E": "It is difficult to understand."}', 'A,B', 26);

-- PART 3 (Q27-30) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_4_id, @TYPE_MC_SINGLE, 'When comparing loneliness to depression, the students', '{"A": "doubt that there will ever be a medical cure for loneliness.", "B": "claim that the link between loneliness and mental health is overstated.", "C": "express frustration that loneliness is not taken more seriously."}', 'A', 27),
(@g3_4_id, @TYPE_MC_SINGLE, 'Why do the students decide to start their presentation with an example from their own experience?', '{"A": "to explain how difficult loneliness can be", "B": "to highlight a situation that most students will recognise", "C": "to emphasise that feeling lonely is more common for men than women"}', 'B', 28),
(@g3_4_id, @TYPE_MC_SINGLE, 'The students agree that talking to strangers is a good strategy for dealing with loneliness because', '{"A": "it creates a sense of belonging.", "B": "it builds self-confidence.", "C": "it makes people feel more positive."}', 'A', 29),
(@g3_4_id, @TYPE_MC_SINGLE, 'The students find it difficult to understand why solitude is considered to be', '{"A": "similar to loneliness.", "B": "necessary for mental health.", "C": "an enjoyable experience."}', 'C', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_id, @TYPE_COMPLETION, 'Rivers became polluted as population grew and 31. $\underline{\hspace{2em}}$ discharged waste.', 'factories', 31),
(@g4_id, @TYPE_COMPLETION, 'River Thames: declared 32. $\underline{\hspace{2em}}$ in 1957; is now cleaner (seals and a 33. $\underline{\hspace{2em}}$ seen).', 'dead', 32),
(@g4_id, @TYPE_COMPLETION, 'River Thames: declared dead in 1957; is now cleaner (seals and a 33. $\underline{\hspace{2em}}$ seen).', 'whale', 33),
(@g4_id, @TYPE_COMPLETION, 'Old warehouses converted into expensive restaurants and 34. $\underline{\hspace{2em}}$ (Los Angeles).', 'apartments', 34),
(@g4_id, @TYPE_COMPLETION, 'Los Angeles River: plan to create a 35. $\underline{\hspace{2em}}$ with sports facilities.', 'park', 35),
(@g4_id, @TYPE_COMPLETION, 'Other cities: proposed facilities for displaying 36. $\underline{\hspace{2em}}$ projects by local people.', 'art', 36),
(@g4_id, @TYPE_COMPLETION, 'Paris (summer): river banks transformed into 37. $\underline{\hspace{2em}}$ (traffic banned).', 'beaches', 37),
(@g4_id, @TYPE_COMPLETION, 'Transport: Over 2 billion passengers use the 38. $\underline{\hspace{2em}}$ in cities.', 'ferry', 38),
(@g4_id, @TYPE_COMPLETION, 'Deliveries: Final stage via cargo 39. $\underline{\hspace{2em}}$ (Amsterdam).', 'bikes', 39),
(@g4_id, @TYPE_COMPLETION, 'Future deliveries: possibly by 40. $\underline{\hspace{2em}}$ (currently not allowed).', 'drone', 40);

-- Assuming a continuation from the previous file or a fresh database run
-- We will insert the new test and use its ID for subsequent inserts.

-- 1️⃣ Insert Test (Cambridge 20 - Test 2)
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 20 - Test 2', 'Academic');
SET @test_id_2 = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id_2, 'Part 1: Council Support for Carers', 'Questions 1–10. Complete the table below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T2S1.m4a', '...[Full Part 1 Transcript]...', 1),
(@test_id_2, 'Part 2: Elmley Town Volunteer Scheme', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T2S2.m4a', '...[Full Part 2 Transcript]...', 2),
(@test_id_2, 'Part 3: Human Geography Assignment', 'Questions 21–30.', 'https://pub-cdba06683f8f64ed78697b31fd99e33e9.r2.dev/audiocam20/T2S3.m4a', '...[Full Part 3 Transcript]...', 3),
(@test_id_2, 'Part 4: Food Trends', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T3S4.m4a', '...[Full Part 4 Transcript]...', 4);

SET @s1_id_2 = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id_2);
SET @s2_id_2 = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id_2);
SET @s3_id_2 = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id_2);
SET @s4_id_2 = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id_2);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Table Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id_2, 'Complete the table below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test2-part1-1.png', 1);
SET @g1_2_id = LAST_INSERT_ID();

-- G2: Part 2 - Matching (Q11-16)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_2, 'What is the role of the volunteers in each of the following activities? Choose SIX answers from the box and write the correct letter, A-I, next to 11-16.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test2-part1-2', 1);
SET @g2_1_2_id = LAST_INSERT_ID();

-- G3: Part 2 - Multiple Choice (Q17-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_2, 'Questions 17-20. Choose the correct letter, A, B or C.', NULL, 2);
SET @g2_2_2_id = LAST_INSERT_ID();

-- G4: Part 3 - Matching (Q21-25)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_2, 'Questions 21-25. What is Rosie and Colin''s opinion about each of the following aspects of human geography? Choose FIVE answers from the box and write the correct letter, A-G.', NULL, 1);
SET @g3_1_2_id = LAST_INSERT_ID();

-- G5: Part 3 - Multiple Choice (Q26-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_2, 'Questions 26-30. Choose the correct letter, A, B or C.', NULL, 2);
SET @g3_2_2_id = LAST_INSERT_ID();

-- G6: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id_2, 'Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test2-part4.png', 1);
SET @g4_2_id = LAST_INSERT_ID();
-- Type IDs: 1 (MC Single), 2 (MC Multi), 3 (Matching), 4 (Completion/Labelling)

-- PART 1 (Q1-10) - Table Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_2_id, 4, 'Carers'' needs: time for other responsibilities and a 1. $\underline{\hspace{2em}}$', 'break', 1),
(@g1_2_id, 4, 'Mother''s needs assessment: The amount of 2. $\underline{\hspace{2em}}$ spent caring each day', 'time', 2),
(@g1_2_id, 4, 'Mother''s needs assessment: Needs help with dressing and getting into the 3. $\underline{\hspace{2em}}$', 'shower', 3),
(@g1_2_id, 4, 'Mother''s needs assessment: Difficulties dealing with 4. $\underline{\hspace{2em}}$', 'money', 4),
(@g1_2_id, 4, 'Aspects of caring that are particularly difficult: loss of 5. $\underline{\hspace{2em}}$', 'memory', 5),
(@g1_2_id, 4, 'Aspects of caring that are particularly difficult: 6. $\underline{\hspace{2em}}$ her', 'lifting', 6),
(@g1_2_id, 4, 'Aspects of caring that are particularly difficult: advice on preventing a 7. $\underline{\hspace{2em}}$', 'fall', 7),
(@g1_2_id, 4, 'Financial support for transport costs (e.g., 8. $\underline{\hspace{2em}}$ to appointments)', 'taxi', 8),
(@g1_2_id, 4, 'Financial support for car-related costs (fuel and 9. $\underline{\hspace{2em}}$)', 'insurance', 9),
(@g1_2_id, 4, 'Other support: help to reduce 10. $\underline{\hspace{2em}}$', 'stress', 10);

-- PART 2 (Q11-16) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_2_id, 3, 'walking around the town centre', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'D', 11),
(@g2_1_2_id, 3, 'helping at concerts', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'I', 12),
(@g2_1_2_id, 3, 'getting involved with community groups', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'H', 13),
(@g2_1_2_id, 3, 'helping with a magazine', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'E', 14),
(@g2_1_2_id, 3, 'participating at lunches for retired people', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'A', 15),
(@g2_1_2_id, 3, 'helping with the website', '{"A": "providing entertainment", "B": "providing publicity about a council service", "C": "contacting local businesses", "D": "giving advice to visitors", "E": "collecting feedback on events", "F": "selling tickets", "G": "introducing guest speakers at an event", "H": "encouraging cooperation between local organisations", "I": "helping people find their seats"}', 'B', 16);

-- PART 2 (Q17-20) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_2_2_id, 1, 'Which event requires the largest number of volunteers?', '{"A": "the music festival", "B": "the science festival", "C": "the book festival"}', 'B', 17),
(@g2_2_2_id, 1, 'What is the most important requirement for volunteers at the festivals?', '{"A": "interpersonal skills", "B": "personal interest in the event", "C": "flexibility"}', 'A', 18),
(@g2_2_2_id, 1, 'New volunteers will start working in the week beginning', '{"A": "2 September.", "B": "9 September.", "C": "23 September."}', 'B', 19),
(@g2_2_2_id, 1, 'What is the next annual event for volunteers?', '{"A": "a boat trip", "B": "a barbecue", "C": "a party"}', 'A', 20);

-- PART 3 (Q21-25) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_2_id, 3, 'Population', '{"A": "The information given about this was too vague.", "B": "This may not be relevant to their course.", "C": "This will involve only a small number of statistics.", "D": "It will be easy to find facts about this.", "E": "The facts about this may not be reliable.", "F": "No useful research has been done on this.", "G": "The information provided about this was interesting."}', 'D', 21),
(@g3_1_2_id, 3, 'Health', '{"A": "The information given about this was too vague.", "B": "This may not be relevant to their course.", "C": "This will involve only a small number of statistics.", "D": "It will be easy to find facts about this.", "E": "The facts about this may not be reliable.", "F": "No useful research has been done on this.", "G": "The information provided about this was interesting."}', 'G', 22),
(@g3_1_2_id, 3, 'Economies', '{"A": "The information given about this was too vague.", "B": "This may not be relevant to their course.", "C": "This will involve only a small number of statistics.", "D": "It will be easy to find facts about this.", "E": "The facts about this may not be reliable.", "F": "No useful research has been done on this.", "G": "The information provided about this was interesting."}', 'B', 23),
(@g3_1_2_id, 3, 'Culture', '{"A": "The information given about this was too vague.", "B": "This may not be relevant to their course.", "C": "This will involve only a small number of statistics.", "D": "It will be easy to find facts about this.", "E": "The facts about this may not be reliable.", "F": "No useful research has been done on this.", "G": "The information provided about this was interesting."}', 'A', 24),
(@g3_1_2_id, 3, 'Poverty', '{"A": "The information given about this was too vague.", "B": "This may not be relevant to their course.", "C": "This will involve only a small number of statistics.", "D": "It will be easy to find facts about this.", "E": "The facts about this may not be reliable.", "F": "No useful research has been done on this.", "G": "The information provided about this was interesting."}', 'E', 25);

-- PART 3 (Q26-30) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_2_2_id, 1, 'Rosie says that in her own city the main problem is', '{"A": "crime.", "B": "housing.", "C": "unemployment."}', 'C', 26),
(@g3_2_2_id, 1, 'What recent additions to the outskirts of their cities are both students happy about?', '{"A": "conference centres", "B": "sports centres", "C": "retail centres"}', 'A', 27),
(@g3_2_2_id, 1, 'The students agree that developing disused industrial sites may', '{"A": "have unexpected costs.", "B": "damage the urban environment.", "C": "destroy valuable historical buildings."}', 'A', 28),
(@g3_2_2_id, 1, 'The students will mention Masdar City as an example of an attempt to achieve', '{"A": "daily collections for waste recycling.", "B": "sustainable energy use.", "C": "free transport for everyone."}', 'B', 29),
(@g3_2_2_id, 1, 'When discussing the ecotown of Greenhill Abbots, Colin is uncertain about', '{"A": "what its objectives were.", "B": "why there was opposition to it.", "C": "how much of it has actually been built."}', 'C', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_2_id, 4, 'Reasons for growth: rise of the smartphone allowed people to instantly share 31. $\underline{\hspace{2em}}$ of food.', 'photos', 31),
(@g4_2_id, 4, 'Influence: Companies which sell 32. $\underline{\hspace{2em}}$ produce were pioneers in using social media influencers to promote products.', 'vegan', 32),
(@g4_2_id, 4, 'Role of Supermarkets: interested in what famous 33. $\underline{\hspace{2em}}$ are putting on their menus.', 'chefs', 33),
(@g4_2_id, 4, 'Avocado Campaign (UK): paid for a group of 34. $\underline{\hspace{2em}}$ to travel to South Africa.', 'journalists', 34),
(@g4_2_id, 4, 'Avocado Campaign (UK): promoted the avocado as beneficial for 35. $\underline{\hspace{2em}}$', 'health', 35),
(@g4_2_id, 4, 'Oat Milk (USA): focused on getting the product into 36. $\underline{\hspace{2em}}$ chains.', 'coffee', 36),
(@g4_2_id, 4, 'Oat Milk (USA): appealed to consumers because of lower impact on the 37. $\underline{\hspace{2em}}$', 'environment', 37),
(@g4_2_id, 4, 'Norwegian Skrei: used to build the 38. $\underline{\hspace{2em}}$ of Norway''s fisheries in general.', 'reputation', 38),
(@g4_2_id, 4, 'Ethical Concerns (Quinoa): demand caused the 39. $\underline{\hspace{2em}}$ to soar, making it unaffordable for local people.', 'price', 39),
(@g4_2_id, 4, 'Ethical Concerns (Quinoa): continuous production decreased the fertility of the 40. $\underline{\hspace{2em}}$', 'soil', 40);

-- Assuming a continuation from the previous file or a fresh database run
-- We will insert the new test and use its ID for subsequent inserts.

-- Set up Type IDs
SET @TYPE_MC_SINGLE = 1;
SET @TYPE_MC_MULTI = 2;
SET @TYPE_MATCHING = 3;
SET @TYPE_COMPLETION = 4;

-- 1️⃣ Insert Test (Cambridge 20 - Test 3)
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 20 - Test 3', 'Academic');
SET @test_id_3 = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id_3, 'Part 1: Furniture Rental Enquiry', 'Questions 1–10. Complete the table below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T3S1.m4a', '...[Full Part 1 Transcript]...', 1),
(@test_id_3, 'Part 2: Bidcuster Archaeology Project', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T3S2.m4a', '...[Full Part 2 Transcript]...', 2),
(@test_id_3, 'Part 3: Theatre Programmes Project', 'Questions 21–30.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T2S3.m4a', '...[Full Part 3 Transcript]...', 3), -- Note: Used T2S3 URL as provided
(@test_id_3, 'Part 4: Inclusive Design', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T3S4.m4a', '...[Full Part 4 Transcript]...', 4);

SET @s1_id_3 = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id_3);
SET @s2_id_3 = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id_3);
SET @s3_id_3 = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id_3);
SET @s4_id_3 = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id_3);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Table Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id_3, 'Complete the table below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test3-part1.png', 1);
SET @g1_3_id = LAST_INSERT_ID();

-- G2: Part 2 - MC Single (Q11-16)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_3, 'Questions 11-16. Choose the correct letter, A, B or C.', NULL, 1);
SET @g2_1_3_id = LAST_INSERT_ID();

-- G3: Part 2 - Map Labelling (Q17-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_3, 'Questions 17-20. Label the map below. Drag the correct letter, A-G, next to Questions 17-20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test3-part2.png', 2);
SET @g2_2_3_id = LAST_INSERT_ID();

-- G4: Part 3 - MC Single (Q21-26)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_3, 'Questions 21-26. Choose the correct letter, A, B or C.', NULL, 1);
SET @g3_1_3_id = LAST_INSERT_ID();

-- G5: Part 3 - Matching (Q27-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_3, 'Questions 27-30. What comment is made about the programme for each of the following shows? Choose FOUR answers from the box and write the correct letter, A-F, next to Questions 27-30.', NULL, 2);
SET @g3_2_3_id = LAST_INSERT_ID();

-- G6: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id_3, 'Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test3-part4.png', 1);
SET @g4_3_id = LAST_INSERT_ID();

-- 4️⃣ Insert Individual Questions

-- PART 1 (Q1-10) - Table Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_3_id, @TYPE_COMPLETION, 'Peak Rentals: Monthly price per room: $105 to $ 1. $\underline{\hspace{2em}}$', '239', 1),
(@g1_3_id, @TYPE_COMPLETION, 'Peak Rentals: Furniture is more 2. $\underline{\hspace{2em}}$', 'modern', 2),
(@g1_3_id, @TYPE_COMPLETION, 'Peak Rentals: Special offer: free 3. $\underline{\hspace{2em}}$', 'lamp', 3),
(@g1_3_id, @TYPE_COMPLETION, '4. $\underline{\hspace{2em}}$ and Oliver: Cheaper than Peak Rentals', 'Aaron', 4),
(@g1_3_id, @TYPE_COMPLETION, 'Aaron and Oliver: Extra 12% charged for 5. $\underline{\hspace{2em}}$', 'damage', 5),
(@g1_3_id, @TYPE_COMPLETION, 'Larch Furniture: Lowest prices for furniture and 6. $\underline{\hspace{2em}}$ equipment', 'electronic', 6),
(@g1_3_id, @TYPE_COMPLETION, 'Larch Furniture: Customer must arrange own 7. $\underline{\hspace{2em}}$', 'insurance', 7),
(@g1_3_id, @TYPE_COMPLETION, '8. $\underline{\hspace{2em}}$ Rentals: Located very near the house', 'Space', 8),
(@g1_3_id, @TYPE_COMPLETION, 'Space Rentals: Use their 9. $\underline{\hspace{2em}}$ to find out about charges', 'app', 9),
(@g1_3_id, @TYPE_COMPLETION, 'Space Rentals: Can request 10. $\underline{\hspace{2em}}$ within one week of delivery', 'exchanges', 10);

-- PART 2 (Q11-16) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_3_id, @TYPE_MC_SINGLE, 'Who was responsible for starting the community project?', '{"A": "the castle owners", "B": "a national charity", "C": "the local council"}', 'B', 11),
(@g2_1_3_id, @TYPE_MC_SINGLE, 'How was the gold coin found?', '{"A": "Heavy rain had removed some of the soil.", "B": "The ground was dug up by wild rabbits.", "C": "A person with a metal detector searched the area."}', 'A', 12),
(@g2_1_3_id, @TYPE_MC_SINGLE, 'What led the archaeologists to believe there was an ancient village on this site?', '{"A": "the lucky discovery of old records", "B": "the bases of several structures visible in the grass", "C": "the unusual stones found near the castle"}', 'A', 13),
(@g2_1_3_id, @TYPE_MC_SINGLE, 'What are the team still hoping to find?', '{"A": "everyday pottery", "B": "animal bones", "C": "pieces of jewellery"}', 'C', 14),
(@g2_1_3_id, @TYPE_MC_SINGLE, 'What was found on the other side of the river to the castle?', '{"A": "the remains of a large palace", "B": "the outline of fields", "C": "a number of small huts"}', 'B', 15),
(@g2_1_3_id, @TYPE_MC_SINGLE, 'What do the team plan to do after work ends this summer?', '{"A": "prepare a display for a museum", "B": "take part in a television programme", "C": "start to organise school visits"}', 'C', 16);

-- PART 2 (Q17-20) - Map Labelling (Type 4)
-- The correct answer is the letter on the map (A-G)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g2_2_3_id, @TYPE_COMPLETION, 'bridge foundations', 'B', 17),
(@g2_2_3_id, @TYPE_COMPLETION, 'rubbish pit', 'A', 18),
(@g2_2_3_id, @TYPE_COMPLETION, 'meeting hall', 'G', 19),
(@g2_2_3_id, @TYPE_COMPLETION, 'fish pond', 'E', 20);

-- PART 3 (Q21-26) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Finn was pleased to discover that their topic', '{"A": "was not familiar to their module leader.", "B": "had not been chosen by other students.", "C": "did not prove to be difficult to research."}', 'B', 21),
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Maya says a mistaken belief about theatre programmes is that', '{"A": "theatres pay companies to produce them.", "B": "few theatre-goers buy them nowadays.", "C": "they contain far more adverts than previously."}', 'A', 22),
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Finn was surprised that, in early British theatre, programmes', '{"A": "were difficult for audiences to obtain.", "B": "were given out free of charge.", "C": "were seen as a kind of contract."}', 'C', 23),
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Maya feels their project should include an explanation of why companies of actors', '{"A": "promoted their own plays.", "B": "performed plays outdoors.", "C": "had to tour with their plays."}', 'A', 24),
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Finn and Maya both think that, compared to nineteenth-century programmes, those from the eighteenth century', '{"A": "were more original.", "B": "were more colourful.", "C": "were more informative."}', 'C', 25),
(@g3_1_3_id, @TYPE_MC_SINGLE, 'Maya doesn\'t fully understand why, in the twentieth century,', '{"A": "very few theatre programmes were printed in the USA.", "B": "British theatre programmes failed to develop for so long.", "C": "theatre programmes in Britain copied fashions from the USA."}', 'B', 26);

-- PART 3 (Q27-30) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_2_3_id, @TYPE_MATCHING, 'Ruy Blas', '{"A": "Its origin is somewhat controversial.", "B": "It is historically significant for a country.", "C": "It was effective at attracting audiences.", "D": "It is included in a recent project.", "E": "It contains insights into the show.", "F": "It resembles an artwork."}', 'F', 27),
(@g3_2_3_id, @TYPE_MATCHING, 'Man of La Mancha', '{"A": "Its origin is somewhat controversial.", "B": "It is historically significant for a country.", "C": "It was effective at attracting audiences.", "D": "It is included in a recent project.", "E": "It contains insights into the show.", "F": "It resembles an artwork."}', 'E', 28),
(@g3_2_3_id, @TYPE_MATCHING, 'The Tragedy of Jane Shore', '{"A": "Its origin is somewhat controversial.", "B": "It is historically significant for a country.", "C": "It was effective at attracting audiences.", "D": "It is included in a recent project.", "E": "It contains insights into the show.", "F": "It resembles an artwork."}', 'B', 29),
(@g3_2_3_id, @TYPE_MATCHING, 'The Sailors\' Festival', '{"A": "Its origin is somewhat controversial.", "B": "It is historically significant for a country.", "C": "It was effective at attracting audiences.", "D": "It is included in a recent project.", "E": "It contains insights into the show.", "F": "It resembles an artwork."}', 'D', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_3_id, @TYPE_COMPLETION, 'Inclusive design: design for everyone, without any 31. $\underline{\hspace{2em}}$ to the original design.', 'adaptation', 31),
(@g4_3_id, @TYPE_COMPLETION, 'Universal design also considers people with 32. $\underline{\hspace{2em}}$ difficulties.', 'cognitive', 32),
(@g4_3_id, @TYPE_COMPLETION, 'Examples in workplaces: Adjustable 33. $\underline{\hspace{2em}}$ for different heights/wheelchair users.', 'desks', 33),
(@g4_3_id, @TYPE_COMPLETION, 'Examples in public toilets: Sensor-activated 34. $\underline{\hspace{2em}}$ are easier for mobility issues.', 'taps', 34),
(@g4_3_id, @TYPE_COMPLETION, 'Examples in tech: Software avoids using shades of 35. $\underline{\hspace{2em}}$ in interfaces (due to vision decline).', 'blue', 35),
(@g4_3_id, @TYPE_COMPLETION, 'Examples in tech: Commands can be made using 36. $\underline{\hspace{2em}}$ access instead of a mouse or keyboard.', 'voice', 36),
(@g4_3_id, @TYPE_COMPLETION, 'Safety issues: Crash tests use male dummy; inadequate for 37. $\underline{\hspace{2em}}$ women.', 'pregnant', 37),
(@g4_3_id, @TYPE_COMPLETION, 'Safety issues: Ill-fitting PPE for women, especially due to smaller 38. $\underline{\hspace{2em}}$ than average men.', 'shoulders', 38),
(@g4_3_id, @TYPE_COMPLETION, 'Safety issues: Problem is worst in the emergency services, particularly the 39. $\underline{\hspace{2em}}$', 'police', 39),
(@g4_3_id, @TYPE_COMPLETION, 'Comfort issues: Standard AC 40. $\underline{\hspace{2em}}$ is often too low for women.', 'temperature', 40);

-- Assuming a continuation from the previous file or a fresh database run

-- Set up Type IDs
SET @TYPE_MC_SINGLE = 1;
SET @TYPE_MC_MULTI = 2;
SET @TYPE_MATCHING = 3;
SET @TYPE_COMPLETION = 4;

-- 1️⃣ Insert Test (Cambridge 20 - Test 4)
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 20 - Test 4', 'Academic');
SET @test_id_4 = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id_4, 'Part 1: Visitors to the City', 'Questions 1–10. Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T4S1.m4a', '...[Full Part 1 Transcript]...', 1),
(@test_id_4, 'Part 2: City Football Club Tour', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T4S2.m4a', '...[Full Part 2 Transcript]...', 2),
(@test_id_4, 'Part 3: Handwriting and Dyspraxia', 'Questions 21–30.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T4S3.m4a', '...[Full Part 3 Transcript]...', 3),
(@test_id_4, 'Part 4: Conflict Between Wildlife and Humans', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/audiocam20/T4S4.m4a', '...[Full Part 4 Transcript]...', 4);

SET @s1_id_4 = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id_4);
SET @s2_id_4 = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id_4);
SET @s3_id_4 = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id_4);
SET @s4_id_4 = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id_4);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Notes Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id_4, 'Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test4-part1.png', 1);
SET @g1_4_id = LAST_INSERT_ID();

-- G2: Part 2 - MC Multi (Q11-12)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_4, 'Questions 11 and 12. Choose TWO letters, A-E. Which TWO things does the speaker say about visiting the football stadium with children?', NULL, 1);
SET @g2_1_4_id = LAST_INSERT_ID();

-- G3: Part 2 - MC Multi (Q13-14)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_4, 'Questions 13 and 14. Choose TWO letters, A-E. Which TWO features of the stadium tour are new this year?', NULL, 2);
SET @g2_2_4_id = LAST_INSERT_ID();

-- G4: Part 2 - Matching (Q15-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_4, 'Questions 15-20. Which event in the history of football in the UK took place in each of the following years? Choose SIX answers from the box and write the correct letter, A-H, next to Questions 15-20.', NULL, 3);
SET @g2_3_4_id = LAST_INSERT_ID();

-- G5: Part 3 - MC Multi (Q21-22)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_4, 'Questions 21 and 22. Choose TWO letters, A-E. Which TWO benefits for children of learning to write did both students find surprising?', NULL, 1);
SET @g3_1_4_id = LAST_INSERT_ID();

-- G6: Part 3 - MC Multi (Q23-24)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_4, 'Questions 23 and 24. Choose TWO letters, A-E. For children with dyspraxia, which TWO problems with handwriting do the students think are easiest to correct?', NULL, 2);
SET @g3_2_4_id = LAST_INSERT_ID();

-- G7: Part 3 - MC Single (Q25-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_4, 'Questions 25-30. Choose the correct letter, A, B or C.', NULL, 3);
SET @g3_3_4_id = LAST_INSERT_ID();

-- G8: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id_4, 'Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam20-test4-part4.png', 1);
SET @g4_4_id = LAST_INSERT_ID();

-- 4️⃣ Insert Individual Questions

-- PART 1 (Q1-10) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_4_id, @TYPE_COMPLETION, 'Hotel name: 1. $\underline{\hspace{2em}}$ Hotel', 'King’s', 1),
(@g1_4_id, @TYPE_COMPLETION, 'Hotel cost: around £ 2. $\underline{\hspace{2em}}$ per night (family room)', '125', 2),
(@g1_4_id, @TYPE_COMPLETION, 'Suggested activities: Recommend doing a 3. $\underline{\hspace{2em}}$ tour of the city centre.', 'walking', 3),
(@g1_4_id, @TYPE_COMPLETION, 'Suggested activities: Visit the old fort by 4. $\underline{\hspace{2em}}$ (half-day trip).', 'boat', 4),
(@g1_4_id, @TYPE_COMPLETION, 'Suggested activities: Science museum closed on Mondays, so 5. $\underline{\hspace{2em}}$ is best.', 'Tuesday', 5),
(@g1_4_id, @TYPE_COMPLETION, 'Suggested activities: Current exhibition about 6. $\underline{\hspace{2em}}$ looks good.', 'space', 6),
(@g1_4_id, @TYPE_COMPLETION, 'Food: Clacton Market has good 7. $\underline{\hspace{2em}}$ food.', 'vegetarian', 7),
(@g1_4_id, @TYPE_COMPLETION, 'Food: Market stores stop serving lunch at 8. $\underline{\hspace{2em}}$', '2.30', 8),
(@g1_4_id, @TYPE_COMPLETION, 'Theatre: Can save up to 9. $\underline{\hspace{2em}}$ percent on some seats (bargain tickets).', '75', 9),
(@g1_4_id, @TYPE_COMPLETION, 'Free activity: Climb Telegraph Hill for a view of the 10. $\underline{\hspace{2em}}$', 'port', 10);

-- PART 2 (Q11-14) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_4_id, @TYPE_MC_MULTI, 'Which TWO things does the speaker say about visiting the football stadium with children?', '{"A": "Children can get their photo taken with a football player.", "B": "There is a competition for children today.", "C": "Parents must stay with their children at all times.", "D": "Children will need sunhats and drinks.", "E": "The café has a special offer on meals for children."}', 'B,C', 11),
(@g2_1_4_id, @TYPE_MC_MULTI, 'Which TWO things does the speaker say about visiting the football stadium with children? (Second Answer)', '{"A": "Children can get their photo taken with a football player.", "B": "There is a competition for children today.", "C": "Parents must stay with their children at all times.", "D": "Children will need sunhats and drinks.", "E": "The café has a special offer on meals for children."}', 'B,C', 12),
(@g2_2_4_id, @TYPE_MC_MULTI, 'Which TWO features of the stadium tour are new this year?', '{"A": "VIP tour", "B": "360 cinema experience", "C": "audio guide", "D": "dressing room tour", "E": "tours in other languages"}', 'A,C', 13),
(@g2_2_4_id, @TYPE_MC_MULTI, 'Which TWO features of the stadium tour are new this year? (Second Answer)', '{"A": "VIP tour", "B": "360 cinema experience", "C": "audio guide", "D": "dressing room tour", "E": "tours in other languages"}', 'A,C', 14);

-- PART 2 (Q15-20) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_3_4_id, @TYPE_MATCHING, '1870', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'D', 15),
(@g2_3_4_id, @TYPE_MATCHING, '1874', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'F', 16),
(@g2_3_4_id, @TYPE_MATCHING, '1875', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'B', 17),
(@g2_3_4_id, @TYPE_MATCHING, '1877', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'H', 18),
(@g2_3_4_id, @TYPE_MATCHING, '1878', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'C', 19),
(@g2_3_4_id, @TYPE_MATCHING, '1880', '{"A": "the introduction of pay for the players", "B": "a change to the design of the goal", "C": "the first use of lights for matches", "D": "the introduction of goalkeepers", "E": "the first international match", "F": "two changes to the rules of the game", "G": "the introduction of fee for spectators", "H": "an agreement on the length of a game"}', 'G', 20);

-- PART 3 (Q21-24) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_4_id, @TYPE_MC_MULTI, 'Which TWO benefits for children of learning to write did both students find surprising?', '{"A": "improved fine motor skills", "B": "improved memory", "C": "improved concentration", "D": "improved imagination", "E": "improved spatial awareness"}', 'C,E', 21),
(@g3_1_4_id, @TYPE_MC_MULTI, 'Which TWO benefits for children of learning to write did both students find surprising? (Second Answer)', '{"A": "improved fine motor skills", "B": "improved memory", "C": "improved concentration", "D": "improved imagination", "E": "improved spatial awareness"}', 'C,E', 22),
(@g3_2_4_id, @TYPE_MC_MULTI, 'For children with dyspraxia, which TWO problems with handwriting do the students think are easiest to correct?', '{"A": "not spacing letters correctly", "B": "not writing in a straight line", "C": "applying too much pressure when writing", "D": "confusing letter shapes", "E": "writing very slowly"}', 'A,C', 23),
(@g3_2_4_id, @TYPE_MC_MULTI, 'For children with dyspraxia, which TWO problems with handwriting do the students think are easiest to correct? (Second Answer)', '{"A": "not spacing letters correctly", "B": "not writing in a straight line", "C": "applying too much pressure when writing", "D": "confusing letter shapes", "E": "writing very slowly"}', 'A,C', 24);

-- PART 3 (Q25-30) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_3_4_id, @TYPE_MC_SINGLE, 'What does the woman say about using laptops to teach writing to children with dyslexia?', '{"A": "Children often lack motivation to learn that way.", "B": "Children become fluent relatively quickly.", "C": "Children react more positively if they make a mistake."}', 'C', 25),
(@g3_3_4_id, @TYPE_MC_SINGLE, 'When discussing whether to teach cursive or print writing, the woman thinks that', '{"A": "cursive writing disadvantages a certain group of children.", "B": "print writing is associated with lower academic performance.", "C": "most teachers in the UK prefer a traditional approach to handwriting."}', 'A', 26),
(@g3_3_4_id, @TYPE_MC_SINGLE, 'According to the students, what impact does poor handwriting have on exam performance?', '{"A": "There is evidence to suggest grades are affected by poor handwriting.", "B": "Neat handwriting is less important now than it used to be.", "C": "Candidates write more slowly and produce shorter answers."}', 'A', 27),
(@g3_3_4_id, @TYPE_MC_SINGLE, 'What prediction does the man make about the future of handwriting?', '{"A": "Touch typing will be taught before writing by hand.", "B": "Children will continue to learn to write by hand.", "C": "People will dislike handwriting on digital devices."}', 'B', 28),
(@g3_3_4_id, @TYPE_MC_SINGLE, 'The woman is concerned that relying on digital devices has made it difficult for her to', '{"A": "take detailed notes.", "B": "spell and punctuate.", "C": "read old documents."}', 'B', 29),
(@g3_3_4_id, @TYPE_MC_SINGLE, 'How do the students feel about their own handwriting?', '{"A": "concerned they are unable to write quickly", "B": "embarrassed by comments made about it", "C": "regretful that they have lost the habit"}', 'C', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_4_id, @TYPE_COMPLETION, 'Birds of prey provide benefits by hunting and consuming rodents (e.g., 31. $\underline{\hspace{2em}}$).', 'rats', 31),
(@g4_4_id, @TYPE_COMPLETION, 'Birds of prey provide benefits by keeping populations of 32. $\underline{\hspace{2em}}$ under control.', 'snakes', 32),
(@g4_4_id, @TYPE_COMPLETION, 'Birds of prey have become important to the community''s economy through 33. $\underline{\hspace{2em}}$', 'tourism', 33),
(@g4_4_id, @TYPE_COMPLETION, 'Threats to birds: accidental deaths (hit by 34. $\underline{\hspace{2em}}$ on roads)', 'traffic', 34),
(@g4_4_id, @TYPE_COMPLETION, 'Threats to birds: electrocuted by high power lines (danger increases during heavy 35. $\underline{\hspace{2em}}$).', 'rain', 35),
(@g4_4_id, @TYPE_COMPLETION, 'Threats to birds: local farmers shoot or 36. $\underline{\hspace{2em}}$ birds to protect chickens.', 'poison', 36),
(@g4_4_id, @TYPE_COMPLETION, 'Ineffective method: keeping chickens safe inside a 37. $\underline{\hspace{2em}}$ (too costly).', 'building', 37),
(@g4_4_id, @TYPE_COMPLETION, 'Effective methods: using a 38. $\underline{\hspace{2em}}$ to scare away predators.', 'dog', 38),
(@g4_4_id, @TYPE_COMPLETION, 'Effective methods: making a loud 39. $\underline{\hspace{2em}}$ (hitting pans).', 'noise', 39),
(@g4_4_id, @TYPE_COMPLETION, 'Overall, farmers use a 40. $\underline{\hspace{2em}}$ of methods.', 'combination', 40);

-- Assuming a continuation from the previous file or a fresh database run

-- Set up Type IDs
SET @TYPE_MC_SINGLE = 1;
SET @TYPE_MC_MULTI = 2;
SET @TYPE_MATCHING = 3;
SET @TYPE_COMPLETION = 4;

-- 1️⃣ Insert Test (Cambridge 19 - Test 1)
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 19 - Test 1', 'Academic');
SET @test_id_5 = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id_5, 'Part 1: Hinchingbrooke Country Park Enquiry', 'Questions 1–10. Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test1%20Part1.mp3', '...[Full Part 1 Transcript]...', 1),
(@test_id_5, 'Part 2: Stanthorpe Twinning Association', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test1%20Part2.mp3', '...[Full Part 2 Transcript]...', 2),
(@test_id_5, 'Part 3: Food Trends Discussion', 'Questions 21–30.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test1%20Part3.mp3', '...[Full Part 3 Transcript]...', 3),
(@test_id_5, 'Part 4: Céide Fields Neolithic Site', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test1%20Part4.mp3', '...[Full Part 4 Transcript]...', 4);

SET @s1_id_5 = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id_5);
SET @s2_id_5 = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id_5);
SET @s3_id_5 = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id_5);
SET @s4_id_5 = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id_5);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Notes Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id_5, 'Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test1-part1.png', 1);
SET @g1_5_id = LAST_INSERT_ID();

-- G2: Part 2 - MC Single (Q11-15)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_5, 'Questions 11–15. Choose the correct letter, A, B or C.', NULL, 1);
SET @g2_1_5_id = LAST_INSERT_ID();

-- G3: Part 2 - Map Labelling (Q16-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_5, 'Questions 16–20. Label the map below. Write the correct letter, A–H, next to Questions 16–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test1-part2.png', 2);
SET @g2_2_5_id = LAST_INSERT_ID();

-- G4: Part 3 - MC Multi (Q21-22)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_5, 'Questions 21 and 22. Choose TWO letters, A–E. Which TWO things did Colin find most satisfying about his bread reuse project?', NULL, 1);
SET @g3_1_5_id = LAST_INSERT_ID();

-- G5: Part 3 - MC Multi (Q23-24)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_5, 'Questions 23 and 24. Choose TWO letters, A–E. Which TWO ways do the students agree that touch-sensitive sensors for food labels could be developed in future?', NULL, 2);
SET @g3_2_5_id = LAST_INSERT_ID();

-- G6: Part 3 - Matching (Q25-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_5, 'Questions 25–30. What is the students’ opinion about each of the following food trends? Choose SIX answers from the box and write the correct answer, A–H, next to Questions 25–30.', NULL, 3);
SET @g3_3_5_id = LAST_INSERT_ID();

-- G7: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id_5, 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test1-part4.png', 1);
SET @g4_5_id = LAST_INSERT_ID();

-- 4️⃣ Insert Individual Questions

-- PART 1 (Q1-10) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_5_id, @TYPE_COMPLETION, 'Park size: 1. $\underline{\hspace{2em}}$ hectares', '69', 1),
(@g1_5_id, @TYPE_COMPLETION, 'Wetland features: Two lakes, several ponds and a 2. $\underline{\hspace{2em}}$', 'stream', 2),
(@g1_5_id, @TYPE_COMPLETION, 'Educational activities: Science (children collect 3. $\underline{\hspace{2em}}$ on plants and insects).', 'data', 3),
(@g1_5_id, @TYPE_COMPLETION, 'Educational activities: Geography (use a 4. $\underline{\hspace{2em}}$ and compass).', 'map', 4),
(@g1_5_id, @TYPE_COMPLETION, 'Educational activities: Leisure and Tourism (focuses on 5. $\underline{\hspace{2em}}$).', 'visitors', 5),
(@g1_5_id, @TYPE_COMPLETION, 'Educational activities: Music (make 6. $\underline{\hspace{2em}}$ using natural materials).', 'sounds', 6),
(@g1_5_id, @TYPE_COMPLETION, 'Benefits of learning outside: Provides a feeling of 7. $\underline{\hspace{2em}}$', 'freedom', 7),
(@g1_5_id, @TYPE_COMPLETION, 'Benefits of learning outside: Develops new 8. $\underline{\hspace{2em}}$', 'skills', 8),
(@g1_5_id, @TYPE_COMPLETION, 'Cost: £ 9. $\underline{\hspace{2em}}$ per child (groups of more than 30).', '4.95', 9),
(@g1_5_id, @TYPE_COMPLETION, 'Cost: No charge for 10. $\underline{\hspace{2em}}$ and accompanying adults.', 'leaders', 10);

-- PART 2 (Q11-15) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_5_id, @TYPE_MC_SINGLE, 'During the visit to Malatte, in France, members especially enjoyed', '{"A": "going to a theme park.", "B": "experiencing a river trip.", "C": "visiting a cheese factory."}', 'B', 11),
(@g2_1_5_id, @TYPE_MC_SINGLE, 'What will happen in Stanthorpe to mark the 25th anniversary of the Twinning Association?', '{"A": "A tree will be planted.", "B": "A garden seat will be bought.", "C": "A footbridge will be built."}', 'A', 12),
(@g2_1_5_id, @TYPE_MC_SINGLE, 'Which event raised most funds this year?', '{"A": "the film show", "B": "the pancake evening", "C": "the cookery demonstration"}', 'B', 13),
(@g2_1_5_id, @TYPE_MC_SINGLE, 'For the first evening with the French visitors host families are advised to', '{"A": "take them for a walk round the town.", "B": "go to a local restaurant.", "C": "have a meal at home."}', 'C', 14),
(@g2_1_5_id, @TYPE_MC_SINGLE, 'On Saturday evening there will be the chance to', '{"A": "listen to a concert.", "B": "watch a match.", "C": "take part in a competition."}', 'A', 15);

-- PART 2 (Q16-20) - Map Labelling (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g2_2_5_id, @TYPE_COMPLETION, 'Farm shop', 'G', 16),
(@g2_2_5_id, @TYPE_COMPLETION, 'Disabled entry', 'C', 17),
(@g2_2_5_id, @TYPE_COMPLETION, 'Adventure playground', 'B', 18),
(@g2_2_5_id, @TYPE_COMPLETION, 'Kitchen gardens', 'D', 19),
(@g2_2_5_id, @TYPE_COMPLETION, 'The Temple of the Four Winds', 'A', 20);

-- PART 3 (Q21-24) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_5_id, @TYPE_MC_MULTI, 'Which TWO things did Colin find most satisfying about his bread reuse project?', '{"A": "receiving support from local restaurants", "B": "finding a good way to prevent waste", "C": "overcoming problems in a basis process", "D": "experimenting with designs and colours", "E": "learning how to apply 3-D priting"}', 'B,D', 21),
(@g3_1_5_id, @TYPE_MC_MULTI, 'Which TWO things did Colin find most satisfying about his bread reuse project? (Second Answer)', '{"A": "receiving support from local restaurants", "B": "finding a good way to prevent waste", "C": "overcoming problems in a basis process", "D": "experimenting with designs and colours", "E": "learning how to apply 3-D priting"}', 'B,D', 22),
(@g3_2_5_id, @TYPE_MC_MULTI, 'Which TWO ways do the students agree that touch-sensitive sensors for food labels could be developed in future?', '{"A": "for use on medical products", "B": "to show that food is no longer fit to eat", "C": "for use with drinks as well as foods", "D": "to provide applications for blind people", "E": "to indicate the weight of certain foods"}', 'A,E', 23),
(@g3_2_5_id, @TYPE_MC_MULTI, 'Which TWO ways do the students agree that touch-sensitive sensors for food labels could be developed in future? (Second Answer)', '{"A": "for use on medical products", "B": "to show that food is no longer fit to eat", "C": "for use with drinks as well as foods", "D": "to provide applications for blind people", "E": "to indicate the weight of certain foods"}', 'A,E', 24);

-- PART 3 (Q25-30) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_3_5_id, @TYPE_MATCHING, 'Use of local products', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'D', 25),
(@g3_3_5_id, @TYPE_MATCHING, 'Reduction in unnecessary packaging', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'G', 26),
(@g3_3_5_id, @TYPE_MATCHING, 'Gluten-free and lactose-free food', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'C', 27),
(@g3_3_5_id, @TYPE_MATCHING, 'Use of branded products related to celebrity chefs', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'B', 28),
(@g3_3_5_id, @TYPE_MATCHING, 'Development of ‘ghost kitchens’ for takeaway food', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'F', 29),
(@g3_3_5_id, @TYPE_MATCHING, 'Use of mushrooms for common health concerns', '{"A": "This is only relevant to young people.", "B": "This may have disappointing results.", "C": "This already seems to be widespread.", "D": "Retailers should do more to encourage this.", "E": "More financial support is needed for this.", "F": "Most people know little about this.", "G": "There should be stricter regulations about this.", "H": "This could be dangerous."}', 'H', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_5_id, @TYPE_COMPLETION, 'Discovery: A local teacher noticed rows of stones (which must be 31. $\underline{\hspace{2em}}$).', 'walls', 31),
(@g4_5_id, @TYPE_COMPLETION, 'Investigation: Patrick Caulfield’s 32. $\underline{\hspace{2em}}$ continued the investigation 40 years later.', 'son', 32),
(@g4_5_id, @TYPE_COMPLETION, 'Investigation: He used a traditional method to map the stones, which was also used for finding 33. $\underline{\hspace{2em}}$', 'fuel', 33),
(@g4_5_id, @TYPE_COMPLETION, 'Preservation: The bog’s soil is saturated with water, and the lack of 34. $\underline{\hspace{2em}}$ helps preserve objects.', 'oxygen', 34),
(@g4_5_id, @TYPE_COMPLETION, 'Neolithic Houses: The houses were 35. $\underline{\hspace{2em}}$ with a hole in the roof to let smoke escape.', 'rectangular', 35),
(@g4_5_id, @TYPE_COMPLETION, 'Neolithic Technology: Pots were used for storage and as 36. $\underline{\hspace{2em}}$ (by filling with fat).', 'lamps', 36),
(@g4_5_id, @TYPE_COMPLETION, 'Land use: Each plot was a suitable size to support an extended 37. $\underline{\hspace{2em}}$', 'family', 37),
(@g4_5_id, @TYPE_COMPLETION, 'Land use: No structures were found for sheltering animals in the 38. $\underline{\hspace{2em}}$', 'winter', 38),
(@g4_5_id, @TYPE_COMPLETION, 'Abandonment: Farming stopped because the 39. $\underline{\hspace{2em}}$ became less productive.', 'soil', 39),
(@g4_5_id, @TYPE_COMPLETION, 'Abandonment: Climatic conditions became wetter due to an increase in 40. $\underline{\hspace{2em}}$', 'rain', 40);

-- Assuming a continuation from the previous file or a fresh database run

-- Set up Type IDs
SET @TYPE_MC_SINGLE = 1;
SET @TYPE_MC_MULTI = 2;
SET @TYPE_MATCHING = 3;
SET @TYPE_COMPLETION = 4;

-- 1️⃣ Insert Test (Cambridge 19 - Test 2)
INSERT INTO listening_test (test_name, test_level) VALUES
('Cambridge 19 - Test 2', 'Academic');
SET @test_id_6 = LAST_INSERT_ID();

-- 2️⃣ Insert Sections
INSERT INTO listening_section (test_id, section_name, intro_text, audio_url, transcript, section_order) VALUES
(@test_id_6, 'Part 1: Guitar Group Enquiry', 'Questions 1–10. Complete the notes below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test2%20Part1.mp3', '...[Full Part 1 Transcript]...', 1),
(@test_id_6, 'Part 2: Working as a Lifeboat Volunteer', 'Questions 11–20.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test2%20Part2.mp3', '...[Full Part 2 Transcript]...', 2),
(@test_id_6, 'Part 3: Recycling Footwear Presentation', 'Questions 21–30.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test2%20Part3.mp3', '...[Full Part 3 Transcript]...', 3),
(@test_id_6, 'Part 4: Tardigrades Research', 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/Audio%20cam%2019/Test4%20Part4.mp3', '...[Full Part 4 Transcript]...', 4);

SET @s1_id_6 = (SELECT section_id FROM listening_section WHERE section_order = 1 AND test_id = @test_id_6);
SET @s2_id_6 = (SELECT section_id FROM listening_section WHERE section_order = 2 AND test_id = @test_id_6);
SET @s3_id_6 = (SELECT section_id FROM listening_section WHERE section_order = 3 AND test_id = @test_id_6);
SET @s4_id_6 = (SELECT section_id FROM listening_section WHERE section_order = 4 AND test_id = @test_id_6);

-- 3️⃣ Insert Question Groups

-- G1: Part 1 - Notes/Table Completion (Q1-10)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s1_id_6, 'Questions 1–6: Complete the form below. Questions 7–10: Complete the table below. Write ONE WORD AND/OR A NUMBER for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test2-part1-1.png, https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test2-part1-2.png', 1);
SET @g1_6_id = LAST_INSERT_ID();

-- G2: Part 2 - MC Single (Q11-16)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_6, 'Questions 11–16. Choose the correct letter, A, B or C.', NULL, 1);
SET @g2_1_6_id = LAST_INSERT_ID();

-- G3: Part 2 - MC Multi (Q17-20)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s2_id_6, 'Questions 17 and 18. Choose TWO letters, A–E. Which TWO things does David say about the lifeboat volunteer training?', NULL, 2),
(@s2_id_6, 'Questions 19 and 20. Choose TWO letters, A–E. Which TWO things does David find most motivating about the work he does?', NULL, 3);
SET @g2_2_6_id = LAST_INSERT_ID();
SET @g2_3_6_id = LAST_INSERT_ID();

-- G4: Part 3 - MC Single (Q21-24)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_6, 'Questions 21–24. Choose the correct letter, A, B or C.', NULL, 1);
SET @g3_1_6_id = LAST_INSERT_ID();

-- G5: Part 3 - Matching (Q25-28)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_6, 'Questions 25–28. What reasons did the recycling manager give for rejecting footwear, according to the students? Choose FOUR answers from the box and write the correct letter, A–F, next to Questions 25–28.', NULL, 2);
SET @g3_2_6_id = LAST_INSERT_ID();

-- G6: Part 3 - MC Single (Q29-30)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s3_id_6, 'Questions 29–30. Choose the correct letter, A, B or C.', NULL, 3);
SET @g3_3_6_id = LAST_INSERT_ID();

-- G7: Part 4 - Notes Completion (Q31-40)
INSERT INTO listening_question_group (section_id, instructions, image_url, group_order) VALUES
(@s4_id_6, 'Questions 31–40. Complete the notes below. Write ONE WORD ONLY for each answer.', 'https://pub-cdba06683f864ed78697b31fd99e33e9.r2.dev/cam19-test2-part4.png', 1);
SET @g4_6_id = LAST_INSERT_ID();

-- 4️⃣ Insert Individual Questions

-- PART 1 (Q1-10) - Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g1_6_id, @TYPE_COMPLETION, 'Coordinator: Gary 1. $\underline{\hspace{2em}}$', 'Mathieson', 1),
(@g1_6_id, @TYPE_COMPLETION, 'Level: 2. $\underline{\hspace{2em}}$', 'beginners', 2),
(@g1_6_id, @TYPE_COMPLETION, 'Place: the 3. $\underline{\hspace{2em}}$', 'college', 3),
(@g1_6_id, @TYPE_COMPLETION, 'Address: 4. $\underline{\hspace{2em}}$ Street', 'New', 4),
(@g1_6_id, @TYPE_COMPLETION, 'Time: Thursday morning at 5. $\underline{\hspace{2em}}$', '11', 5),
(@g1_6_id, @TYPE_COMPLETION, 'Recommended website: ‘The perfect 6. $\underline{\hspace{2em}}$’', 'instrument', 6),
(@g1_6_id, @TYPE_COMPLETION, 'Tuning guitars – using an app or by 7. $\underline{\hspace{2em}}$', 'ear', 7),
(@g1_6_id, @TYPE_COMPLETION, 'Strumming chords using our thumbs – keeping time while the teacher is 8. $\underline{\hspace{2em}}$', 'clapping', 8),
(@g1_6_id, @TYPE_COMPLETION, 'Playing songs – often listening to a 9. $\underline{\hspace{2em}}$ of a song', 'recording', 9),
(@g1_6_id, @TYPE_COMPLETION, 'Playing single notes and simple tunes – playing together, then 10. $\underline{\hspace{2em}}$', 'alone', 10);

-- PART 2 (Q11-16) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_1_6_id, @TYPE_MC_SINGLE, 'What made David leave London and move to Northsea?', '{"A": "He was eager to develop a hobby.", "B": "He wanted to work shorter hours.", "C": "He found his job in website design unsatisfying."}', 'A', 11),
(@g2_1_6_id, @TYPE_MC_SINGLE, 'The Lifeboat Institution in Northsea was built with money provided by', '{"A": "a local organisation.", "B": "a local resident.", "C": "the local council."}', 'B', 12),
(@g2_1_6_id, @TYPE_MC_SINGLE, 'In his health assessment, the doctor was concerned about the fact that David', '{"A": "might be colour blind.", "B": "was rather short-sighted.", "C": "had undergone eye surgery."}', 'A', 13),
(@g2_1_6_id, @TYPE_MC_SINGLE, 'After arriving at the lifeboat station, they aim to launch the boat within', '{"A": "five minutes.", "B": "six to eight minutes.", "C": "eight and a half minutes."}', 'B', 14),
(@g2_1_6_id, @TYPE_MC_SINGLE, 'As a ‘helmsman’, David has the responsibility of deciding', '{"A": "who will be the members of his crew.", "B": "what equipment it will be necessary to take.", "C": "if the lifeboat should be launched."}', 'C', 15),
(@g2_1_6_id, @TYPE_MC_SINGLE, 'As well as going out on the lifeboat, David', '{"A": "gives talks on safety at sea.", "B": "helps with fundraising.", "C": "recruits new volunteers."}', 'A', 16);

-- PART 2 (Q17-20) - Multiple Choice (Type 2)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g2_2_6_id, @TYPE_MC_MULTI, 'Which TWO things does David say about the lifeboat volunteer training?', '{"A": "The residential course developed his leadership skills.", "B": "The training in use of ropes and knots was quite brief.", "C": "The training exercises have built up his mental strength.", "D": "The casualty care activities were particularly challenging for him.", "E": "The wave tank activities provided practice in survival techniques."}', 'C,E', 17),
(@g2_2_6_id, @TYPE_MC_MULTI, 'Which TWO things does David say about the lifeboat volunteer training? (Second Answer)', '{"A": "The residential course developed his leadership skills.", "B": "The training in use of ropes and knots was quite brief.", "C": "The training exercises have built up his mental strength.", "D": "The casualty care activities were particularly challenging for him.", "E": "The wave tank activities provided practice in survival techniques."}', 'C,E', 18),
(@g2_3_6_id, @TYPE_MC_MULTI, 'Which TWO things does David find most motivating about the work he does?', '{"A": "working as part of a team", "B": "experiences when working in winter", "C": "being thanked by those he has helped", "D": "the fact that it keeps him fit", "E": "the chance to develop new equipment"}', 'A,B', 19),
(@g2_3_6_id, @TYPE_MC_MULTI, 'Which TWO things does David find most motivating about the work he does? (Second Answer)', '{"A": "working as part of a team", "B": "experiences when working in winter", "C": "being thanked by those he has helped", "D": "the fact that it keeps him fit", "E": "the chance to develop new equipment"}', 'A,B', 20);

-- PART 3 (Q21-24) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_1_6_id, @TYPE_MC_SINGLE, 'At first, Don thought the topic of recycling footwear might be too', '{"A": "limited in scope.", "B": "hard to research.", "C": "boring for listeners."}', 'A', 21),
(@g3_1_6_id, @TYPE_MC_SINGLE, 'When discussing trainers, Bella and Don disagree about', '{"A": "how popular they are among young people.", "B": "how suitable they are for school.", "C": "how quickly they wear out."}', 'B', 22),
(@g3_1_6_id, @TYPE_MC_SINGLE, 'Bella says that she sometimes recycles shoes because', '{"A": "they no longer fit.", "B": "she no longer likes them.", "C": "they are no longer in fashion."}', 'B', 23),
(@g3_1_6_id, @TYPE_MC_SINGLE, 'What did the article say that confused Don?', '{"A": "Public consumption of footwear has risen.", "B": "Less footwear is recycled now than in the past.", "C": "People dispose of more footwear than they used to."}', 'B', 24);

-- PART 3 (Q25-28) - Matching (Type 3)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_2_6_id, @TYPE_MATCHING, 'the high-heeled shoes', '{"A": "one shoe was missing", "B": "the colour of one shoe had faded", "C": "one shoe had a hole in it", "D": "the shoes were brand new", "E": "the shoes were too dirty", "F": "the stitching on the shoes was broken"}', 'E', 25),
(@g3_2_6_id, @TYPE_MATCHING, 'the ankle boots', '{"A": "one shoe was missing", "B": "the colour of one shoe had faded", "C": "one shoe had a hole in it", "D": "the shoes were brand new", "E": "the shoes were too dirty", "F": "the stitching on the shoes was broken"}', 'B', 26),
(@g3_2_6_id, @TYPE_MATCHING, 'the baby shoes', '{"A": "one shoe was missing", "B": "the colour of one shoe had faded", "C": "one shoe had a hole in it", "D": "the shoes were brand new", "E": "the shoes were too dirty", "F": "the stitching on the shoes was broken"}', 'A', 27),
(@g3_2_6_id, @TYPE_MATCHING, 'the trainers', '{"A": "one shoe was missing", "B": "the colour of one shoe had faded", "C": "one shoe had a hole in it", "D": "the shoes were brand new", "E": "the shoes were too dirty", "F": "the stitching on the shoes was broken"}', 'C', 28);

-- PART 3 (Q29-30) - Multiple Choice (Type 1)
INSERT INTO listening_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@g3_3_6_id, @TYPE_MC_SINGLE, 'Why did the project to make ‘new’ shoes out of old shoes fail?', '{"A": "People believed the ‘new’ pairs of shoes were unhygienic.", "B": "There were not enough good parts to use in the old shoes.", "C": "The shoes in the ‘new’ pairs were not completely alike."}', 'C', 29),
(@g3_3_6_id, @TYPE_MC_SINGLE, 'Bella and Don agree that they can present their topic', '{"A": "from a new angle.", "B": "with relevant images.", "C": "in a straightforward way"}', 'A', 30);

-- PART 4 (Q31-40) - Notes Completion (Type 4)
INSERT INTO listening_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@g4_6_id, @TYPE_COMPLETION, 'Also known as water ‘bears’ (due to how they 31. $\underline{\hspace{2em}}$) and ‘moss piglets’', 'move', 31),
(@g4_6_id, @TYPE_COMPLETION, 'Physical traits: A 32. $\underline{\hspace{2em}}$ round body and four pairs of legs', 'short', 32),
(@g4_6_id, @TYPE_COMPLETION, 'Physical traits: Claws or 33. $\underline{\hspace{2em}}$ for gripping', 'discs', 33),
(@g4_6_id, @TYPE_COMPLETION, 'Physical traits: Body filled with a liquid that carries both 34. $\underline{\hspace{2em}}$ and blood', 'oxygen', 34),
(@g4_6_id, @TYPE_COMPLETION, 'Physical traits: Mouth shaped like a 35. $\underline{\hspace{2em}}$ with teeth called stylets', 'tube', 35),
(@g4_6_id, @TYPE_COMPLETION, 'Resilience: Very resilient and can exist in very low or high 36. $\underline{\hspace{2em}}$', 'temperatures', 36),
(@g4_6_id, @TYPE_COMPLETION, 'State of cryptobiosis: A type of 37. $\underline{\hspace{2em}}$ ensures their DNA is not damaged.', 'protein', 37),
(@g4_6_id, @TYPE_COMPLETION, 'State of cryptobiosis: Research is underway to find out how many days they can stay alive in 38. $\underline{\hspace{2em}}$', 'space', 38),
(@g4_6_id, @TYPE_COMPLETION, 'Diet: Consume liquids, e.g., those found in moss or 39. $\underline{\hspace{2em}}$', 'seaweed', 39),
(@g4_6_id, @TYPE_COMPLETION, 'Conservation status: They are not considered to be 40. $\underline{\hspace{2em}}$', 'endangered', 40);

-- Assuming Test ID 1 was assigned to Cambridge 20 - Test 1
SET @T1_ID = 1;

UPDATE listening_section SET transcript = 'WOMAN: I’ve been meaning to ask you for some advice about restaurants. I need to book somewhere to celebrate my sister’s 30th birthday, and I liked the sound of that place you went to for your mum’s 50th. MAN: The Junction. Yeah, I’d definitely recommend that for a special occasion. We had a great time there. Everyone really enjoyed it. WOMAN: Where is it again? I can’t remember. MAN: It is on Grayson Street only about a two walk from the station. WOMAN: Oh that’s good I prefer not to have to drive anywhere but I don’t want to have to walk too far either. MAN: Yes, the location’s perfect, but that’s not necessarily why I’d recommend it. The food’s amazing. If you like fish, it’s probably the best restaurant in town for that. It’s always really fresh and there are lots of interesting dishes to choose from But all the food is good there. WOMAN: Is it really expensive? MAN: It’s certainly not cheap, but for a special occasion I think it’s fine. It’s got a great atmosphere and before dinner you can go up on the roof and have a drink. It’s really nice up there, but you need to book. It’s very popular as the views are spectacular. WOMAN: Sounds good. So that’s definitely a possibility then. Is there anywhere else you can think of? MAN: If you want somewhere a bit less formal, then you could try Paloma. WOMAN: Where’s that? I haven’t heard of it. MAN: No, it’s quite new. It’s only been open a few months, but it’s got a great reputation already. It’s in a really beautiful old building on Bow Street. WOMAN: Oh, I think I know where you mean. Right beside the cinema. MAN: Yes, that’s it. I’ve only been there a couple of times, but I was really impressed. The chef used to work at Don Felipe’s, apparently. I was really sorry when that closed down. WOMAN: So is all the food they serve Spanish, then? MAN: Yeah. You can get lots of small dishes to share, which always works really well if you’re in a group. WOMAN: Hmm. Worth thinking about. MAN: Yeah. There’s a lively atmosphere and the waiters are really friendly. The only thing is that you need to pay a £50 deposit to book a table. WOMAN: A lot of restaurants are doing that these days. I should have a look at the menu to check if there is a good choice of vegetarian dishes. A couple of my friends have stopped eating meat MAN: Not sure I say the selection of those would be quite limited. Before you hear the rest of the conversation, you have some time to look at questions five to ten. Now listen and answer questions 5 to 10. MAN: I’ve just thought of another idea. Have you been to the Audley? WOMAN: No, don’t think I’ve heard of it. How’s it spelt? MAN: A-U-D-L-E-Y. You must have heard of it. There’s been a lot about it in the press. WOMAN: I don’t tend to pay much attention to that kind of thing. So where is it exactly? MAN: It’s in that hotel near Baxter Bridge, on the top floor. WOMAN: Oh, the views would be incredible from up there. MAN: Yeah, I’d love to go. I can’t think of the chef’s name, but she was a judge on that TV cookery show recently. And she’s written a couple of cookery books. WOMAN: Oh, Angela Frayne. MAN: That’s the one. Anyway, it had excellent reviews from all the newspapers. WOMAN: She only likes cooking with local products, doesn’t she? MAN: Yes, everything… has to be sourced locally. WOMAN: Ah, right. And is it just really tiny portions? MAN: I imagine they’d be average. WOMAN: Oh, I see. And what about the cost? MAN: Set lunch… £30 a head. In the evening, I think it’d be more like £50. WOMAN: That’s a bit steep. Well, I’ve got plenty of options now. Thanks for your help, Tom. MAN: You’re welcome.' WHERE section_order = 1 AND test_id = @T1_ID;

UPDATE listening_section SET transcript = 'Hello and welcome. My name’s Heather McCallum and I’m one of the potters who work here at Edelman Pottery. Before we go into the workshop, I just want to say a bit about the craft of pottery. Then we’ll have a look at the equipment and you can try making a pot of your own. Like many people, I’m sure you know that pottery as an art form is tens of thousands of years old. And we know this because it stands the test of time. Things like baskets and pictures don’t survive on the earth in the same way that pots do. and even if ancient pots are found in small pieces they still provide a lot of information about the past. There is no doubt that pottery has given archaeologists a fascinating insight into how ancient hills lived. The shape of an artefact may have been lost but archaeologists can tell whether the pots were for, say, storage or cooking by examining the impressions on the clay, the scratches from tools, and the clay itself can reveal where the pots came from. When I ask people why they want to take a pottery class with me, they sometimes talk about these things. Like our ancestors, they hope that something they create will also last longer than they do, that their work, whether it is good or not, might say something about humanity many years after their death. Of course, you will all have your own reasons for coming here. As far as I’m concerned, what I love most is the concentration you need to make a good pot. That focus takes you away from the stresses of everyday life. If you’re elderly, it’s also good exercise for hands and wrists and helps with arthritis. And of course, it’s a fun activity for children because it’s so messy. Here at Edelman Pottery, we show you some of the basic pottery techniques so that you can use these to create whatever you wish. A gift for a friend, perhaps. Like nearly everyone who comes here, I’m sure this is the first time you will have tried the art so we’ll keep things simple today. Now, before we move on, can I just say a word about what you’re wearing? As we said in our email please remove any watches, necklaces etc and put them somewhere safe. If you have long hair do tie it back now. We provide aprons later but I trust your clothes are old but comfortable, not your favourite T-shirt or jeans. So now we’re in the workshop. Have a look around. There’s a lot going on. To make pottery that will last, you need a potter’s wheel, a kiln, which is basically a very hot oven where you fire the pottery, and some tools. So, first, the kiln. If you look over in the far corner, you’ll see one of ours. Since their invention, kilns have changed very little, though in the past 20 years a lot of progress has been made in temperature control. Basically, a kiln removes the water from clay at temperatures of around 1000 degrees Celsius. This allows anything you’ve made to set permanently in shape. It’s a pretty ugly heavy object that’s hard to keep in a house or flat, so most people don’t have one. You may think, can’t I use my oven? Well, that’s possible, but domestic ovens don’t really get hot enough and eventually the clay will crack and fall apart. Some people fire pottery in a fire pit outside but bear in mind… that can be dangerous. You also need to know about safety procedures for kilns as they release toxic compounds into the air. Every potter needs a potter’s wheel. This machine is used to shape the clay into an object with circular walls or sides, such as a bowl. Its invention revolutionised the pottery industry, allowing multiple items to be produced in a day. Lastly, there are a number of different tools that potters use, depending on what they want to make. When you start, your hands can make all kinds of shapes and curves without relying on a sculpting tool. However, there are some basic tools that you will need to handle the clay on the wheel. Some look very strange and have even odder names that you may find hard to remember. Rather than go through them all now, I’ll just name a few tools as we go along. We can provide these and I wouldn’t recommend spending money on them yet. So, let’s try making a pot of your own. If you sit down…' WHERE section_order = 2 AND test_id = @T1_ID;

UPDATE listening_section SET transcript = 'TAMARA: Shall we go through the notes we’ve made from our research into loneliness now, Dev? DEV: OK, Tamara. It’s been a real eye-opener. I had no idea that loneliness has been increasing steadily for the last 20 years. TAMARA: I know. And it’s the same all over the world. The downside of a modern lifestyle, I guess. DEV: Did you come to any conclusions about what the reasons for the increase are? TAMARA: Well, I’d assumed it was mainly an issue for the elderly, but in fact it’s something which affects young people just as much. DEV: So nothing really to do with longer lifespans. What about social media? In my case, far from making me feel isolated, it actually does the opposite. doesn’t it? TAMARA: It definitely does more good than harm. I’d say loneliness has a lot to do with the way cities are designed. People living in high flats with not much opportunity to speak to their neighbours. DEV: I think you’re right. TAMARA: Another possible reason is that people are having fewer children and don’t live in large extended family groups. DEV: But in this country anyway, that all changed decades ago. And yet loneliness is a more recent problem. TAMARA: I suppose so. A more plausible explanation is that people are having to move around for work and often end up living miles away from their family and friends. DEV: That’s true. TAMARA: Looking at the studies on health risks and loneliness, there are claims that loneliness has as much impact as smoking 15 cigarettes a day. DEV: Or similar to the risks caused by obesity. But I’m not sure there’s enough evidence for some of these claims. TAMARA: Well, what about that one in Finland, which showed that loneliness increased the risk of cancer by about 10%? And those findings have been supported by other studies too. DEV: You’re right about that one. I was actually thinking of the studies on dementia. Some found no association between loneliness and dementia, and others found the opposite. TAMARA: Not exactly reliable, then. There’s been a lot of research on cardiovascular disease and whether loneliness contributes to that. DEV: Yes, I read that it was hard to reach a judgment, as the definition of loneliness varied quite a lot, and the responses from participants were too subjective. But there’s no doubt that loneliness contributes to a weakened immune system. TAMARA: Unquestionably. The data on that is sound. DEV: What did you think about the evolutionary theory of loneliness? TAMARA: Well, I thought the idea that loneliness evolved because it motivated people to be with other people is quite convincing. Survival often depended on group cooperation. DEV: But I don’t think there is enough evidence to claim that there must be a group of neurons in our brains which influence social behaviour by making us feel bad when we’re alone. TAMARA: There are a few studies which support the theory, but not conclusively enough. More evidence is needed. DEV: And anyway, this theory’s not really useful when it comes to solving the problem of loneliness today. TAMARA: True. DEV: Should we look at the relationship between loneliness and mental health now? TAMARA: OK. So, loneliness and depression are clearly related and that’s been recognised by various governments around the world. But unlike depression, loneliness has no recognised clinical form. DEV: There’s no available diagnosis or effective treatment and that’s not likely to change. TAMARA: I don’t think so either. I was thinking we should start our presentation with an example from our own experience. I’d like to talk about how lonely I was when I started university – being away from home for the first time and all that. DEV: Good idea. Everyone will be able to relate to that, although a lot of students were probably too embarrassed to admit to it. TAMARA: Yeah. We could discuss ways of dealing with loneliness as well, like just talking to strangers. DEV: Loads of studies have shown that interactions with shop assistants and bar staff make people feel more optimistic and relaxed. TAMARA: I don’t know about that, but it must make people feel more connected with their community. DEV: True, although you need to be a certain kind of person to be able to just strike up a conversation. TAMARA: Good point. We should say something about solitude and how being alone and being lonely aren’t the same thing. It’s strange the way some people can’t stand being by themselves while others love it. DEV: Yeah, the research shows a certain amount of solitude is beneficial for wellbeing, which I appreciate, but being alone isn’t something I actually like. I’d never choose to go on holiday alone, for example. TAMARA: Me neither. DEV: Well, let’s not… you' WHERE section_order = 3 AND test_id = @T1_ID;

UPDATE listening_section SET transcript = 'It’s quite hard to think of a city that doesn’t have a big river running through it. If you think about the major cities in the world, Shanghai, New York, Mumbai, London, they’re nearly all built on rivers. When these cities were established hundreds or even thousands of years ago, the rivers were a big part of people’s lives. In 16th century London, the quickest way to get from one part of the city to another was by river. But people also used the river for fishing, as the water then was relatively clean, and they would also go on boat trips up and down the river just for pleasure, as a relaxing escape from the noise and bustle of the city streets. But as industries developed and populations increased city rivers suffered The rising number of people meant there was a huge increase in the amount of sewage discharged into the rivers. Rivers had always been used for this purpose, but when the number of inhabitants was so small, that wasn’t such a problem. However, as cities grew to over a million inhabitants, the impact on the rivers became more serious. In addition, other types of pollution increased, as factories were built beside the river and discharged their waste materials into the water. This got worse over time. As recently as 1957, scientists at London’s Natural History Museum declared that the River Thames was dead in biological terms, as the water was too filthy to support any kind of life. But in recent years, as rivers lost their industrial function, cities have begun to recognise their true value and to take steps to clean them up. For example, the River Thames is now cleaner than it’s been for 150 years. These days you can see seals swimming in the water, and recently people had to try to rescue a whale, which had got lost and swam up the river from the sea by mistake. Unfortunately, they didn’t succeed, but the problem was disorientation rather than the quality of the water. Then, all around the world, riverside areas are now seen as prime sites for development. Warehouses that were once used for storing goods are now being converted into expensive restaurants and also into apartments with river views, which are in great demand and sell for astronomical prices. In Los Angeles, on the west coast of the USA, an architect has plans to revitalise the banks of the river and to make a park there which can provide facilities for sports as well as a natural environment for relaxing in. It also hoped that the riverside could be used for other purposes. It’s been proposed that facilities could be provided for displaying projects related to various kinds of art that have been produced by local people, for example. In the city of Paris, During the summer months of July and August, all the traffic is banned from the roads by the sides of the river, and the banks are transformed into beaches, where people can relax in deck chairs under potted palm trees, sunbathe or buy a drink or a snack while enjoying the view. But to make the most of our rivers in our increasingly crowded cities, we need to allow them to regain their original purpose and be used as a means of transport, reclaiming our streets from cars and lorries. To do this, we’ll have to shift more traffic back to the river, but this time cleanly and silently, making the most of modern technology. Already, more than two billion passengers use the ferry to travel in cities around the world, like Istanbul, San Francisco and New York, and these numbers are set to rise further. Admittedly, it’s not a fast way of travelling, but neither is a car when it’s stuck in traffic. Of course, passenger traffic on roads might decrease as more people start working from home, but another recent development, the huge rise in online shopping, has meant that another form of urban traffic just keeps on growing, and that’s deliveries. Trucks and vans in the city pollute and double-park while dropping off parcels. Imagine using the immense capacity of shipping to take these trucks off the road. One freight barge can replace 44 large trucks, uses far less energy and causes less pollution When the barge docks at the riverside, the parcels could be taken the last few kilometres to their final destination on cargo bikes, electric ones of course. This is already happening in the Dutch city of Amsterdam, and in future the final stage could even be carried out by drone, although at present this isn’t allowed. Wouldn’t it be great to unblock our city centres in this way? Looking further ahead…' WHERE section_order = 4 AND test_id = @T1_ID;

-- Assuming Test ID 2 was assigned to Cambridge 20 - Test 2
SET @T2_ID = 2;

UPDATE listening_section SET transcript = 'WOMAN: It’s really good to see you, Tom. Since I had to give up work, I feel I’m losing touch with my friends and colleagues. MAN: We really miss you in the office. We were all so sorry you had to leave. But you must be relieved to have more time to look after your mother. How is she? WOMAN: Well she is very cheerful, but she needs a lot of help. MAN: Have you tried to get any support from the local council? WOMAN: No, I didn’t know I could. MAN: Yes, they offer different kinds of practical support. They realise that carers sometimes need time for all the other responsibilities they have, apart from the person they’re caring for, and also that they sometimes need a break. WOMAN: Absolutely. OK, so tell me more. How do I go about getting this support? MAN: Well, you’d have to have an assessment of your mother’s needs. That means someone would come round and talk to you about the situation and what you need. So, for a start, they’d want to know the amount of time you spend looking after your mother every day. WOMAN: OK. MAN: Then they’ll probably ask you what sorts of tasks you do for your mother during the day. Things like if she needs help with getting dressed, for example. WOMAN: Right. I help her with that. And also I help her get into the shower in the morning. MAN: Yes, that sort of thing. They’ll probably ask you if you do the shopping for her and help her at mealtimes, and whether she can cope using money. WOMAN: Yes, that’s becoming a bit of a problem. She used to be very good at it, but not anymore. MAN: And be ready to tell them about anything you find particularly difficult about caring for your mother. WOMAN: So, recently I’ve noticed she’s started to have quite bad problems with her memory. If I wasn’t there, I think she’d forget to eat, for example. And often she doesn’t seem quite sure what day it is. MAN: Yes, tell them about that. And are there any physical difficulties you have caring for her? Lifting her, for example? WOMAN: Yes, she’s quite heavy and I’m afraid of hurting my back. I’d be in real trouble if that happened. MAN: They can give you advice about that and also about how to avoid the possibility of your mum having a fall. WOMAN: Great. So once they’ve done this assessment, if I’m eligible, what happens next? MAN: Well, they might support you financially. So they might help you with transport costs, like if you have to get a taxi to take your mother for an appointment, for example. WOMAN: I usually drive her myself, actually. So, could I claim the petrol? MAN: You could. And you can claim for the insurance too. WOMAN: Oh, right. MAN: And if you need help with the housework, they can arrange for someone to come along once or twice a week. And one other thing. I hope you don’t mind me saying this, but it important you look after yourself And it seems to me you under quite a bit of stress WOMAN: I am yes MAN: Well tell the council because they may be able to give you some advice on how to minimise it WOMAN: Really? Though, actually, I feel so much better having talked to you. I’ll get in touch with the council straight away. Now, shall we go for coffee or something?' WHERE section_order = 1 AND test_id = @T2_ID;

UPDATE listening_section SET transcript = 'Good morning everyone. I’m Steve Wainwright from Elmley Town Council and I organise the town’s volunteer scheme. I’m delighted you’re all interested in joining the scheme. Our volunteers help to create a sense of community among the many people who live in our historic town of Elmley and make residents and visitors feel welcome at local events. First, I’ll mention just a few of the activities that volunteers carry out. One is to walk around the town centre streets wearing our volunteer T-shirt. Tourists often ask how to get to a particular shop, and they might also be grateful for recommendations about what to visit. The town holds a large number of concerts each year and part of the volunteers’ role is to get everyone in the audience to the right place as smoothly as possible. You’d be surprised how many people buy tickets, then don’t check them, and head for the wrong section of the hall. Volunteers may get involved with community groups, such as sports clubs or gardeners’ associations. Here, the volunteers talk about how groups can help each other. For instance, a writing group might want to travel to another town to hear a talk by a well-known author, but may not know that another club has a coach they could travel in. The town produces a monthly magazine, and anyone who lives in the town can send in articles. It’s free to residents and is paid for by local businesses. That’s the responsibility of the council’s advertising department, we depend on volunteers though to find out what people think of events they’ve attended and any suggestions they have for the future. The volunteers then send a summary to the editors. There are a number of clubs for retired people and every year the council arranges lunch for all the members. The volunteers welcome the guests and when everyone’s sitting down and relaxing after the meal, some volunteers put on a show. Usually around half an hour of songs and short plays, the club members really welcome the chance to chat to the volunteers at these events. The town council has a website, of course, and volunteers are asked to help by making sure residents know about it. It’s updated every day with information about future activities and we want as many people as possible to use it. OK, now you know some of the things our volunteers do, I’ll go on to some practical matters. As you probably know, the town arranges three major festivals every year, and they all depend on a large number of volunteers. The book festival lasts three days, and uses several venues which all need volunteers. More are needed for the music festival because that lasts a whole week, and even more help is required for the science festival, even though it’s only two days long. It involves quite a lot of venues, though. It’s a good idea to help at the festival you’re most interested in, because you can attend most of the events for free. We try to use volunteers who are flexible, though, because some festival events are held outdoors and the weather may affect the size of the audience and even whether the event can take place so there can be changes at short notice. What is essential though is being able to get on well with other people and also to deal with someone who’s behaving badly, as occasionally happens. Our plan is to get you all working in September, after a week’s training starting on the 2nd, so we’ll be timetabling you for duties the following week from the 9th onward. Later in the week beginning September 23rd we have a chat with each of you to find out how you feel about being a volunteer and what extra support you need. As a thank you to the volunteers we arrange an annual event. In recent years we’ve had a party in the Town Hall and last year a barbecue in Chamber Park. Our forthcoming event is a trip along the canal from here to Dewhurst and back. It’s on Saturday, September the 28th. And if you’d like to attend, you can sign up once you start work. Now, this is the Volunteer’s T-shirt.' WHERE section_order = 2 AND test_id = @T2_ID;

UPDATE listening_section SET transcript = 'TAMARA: Shall we go through the notes we’ve made from our research into loneliness now, Dev? DEV: OK, Tamara. It’s been a real eye-opener. I had no idea that loneliness has been increasing steadily for the last 20 years. TAMARA: I know. And it’s the same all over the world. The downside of a modern lifestyle, I guess. DEV: Did you come to any conclusions about what the reasons for the increase are? TAMARA: Well, I’d assumed it was mainly an issue for the elderly, but in fact it’s something which affects young people just as much. DEV: So nothing really to do with longer lifespans. What about social media? In my case, far from making me feel isolated, it actually does the opposite. doesn’t it? TAMARA: It definitely does more good than harm. I’d say loneliness has a lot to do with the way cities are designed. People living in high flats with not much opportunity to speak to their neighbours. DEV: I think you’re right. TAMARA: Another possible reason is that people are having fewer children and don’t live in large extended family groups. DEV: But in this country anyway, that all changed decades ago. And yet loneliness is a more recent problem. TAMARA: I suppose so. A more plausible explanation is that people are having to move around for work and often end up living miles away from their family and friends. DEV: That’s true. TAMARA: Looking at the studies on health risks and loneliness, there are claims that loneliness has as much impact as smoking 15 cigarettes a day. DEV: Or similar to the risks caused by obesity. But I’m not sure there’s enough evidence for some of these claims. TAMARA: Well, what about that one in Finland, which showed that loneliness increased the risk of cancer by about 10%? And those findings have been supported by other studies too. DEV: You’re right about that one. I was actually thinking of the studies on dementia. Some found no association between loneliness and dementia, and others found the opposite. TAMARA: Not exactly reliable, then. There’s been a lot of research on cardiovascular disease and whether loneliness contributes to that. DEV: Yes, I read that it was hard to reach a judgment, as the definition of loneliness varied quite a lot, and the responses from participants were too subjective. But there’s no doubt that loneliness contributes to a weakened immune system. TAMARA: Unquestionably. The data on that is sound. DEV: What did you think about the evolutionary theory of loneliness? TAMARA: Well, I thought the idea that loneliness evolved because it motivated people to be with other people is quite convincing. Survival often depended on group cooperation. DEV: But I don’t think there is enough evidence to claim that there must be a group of neurons in our brains which influence social behaviour by making us feel bad when we’re alone. TAMARA: There are a few studies which support the theory, but not conclusively enough. More evidence is needed. DEV: And anyway, this theory’s not really useful when it comes to solving the problem of loneliness today. TAMARA: True. DEV: Should we look at the relationship between loneliness and mental health now? TAMARA: OK. So, loneliness and depression are clearly related and that’s been recognised by various governments around the world. But unlike depression, loneliness has no recognised clinical form. DEV: There’s no available diagnosis or effective treatment and that’s not likely to change. TAMARA: I don’t think so either. I was thinking we should start our presentation with an example from our own experience. I’d like to talk about how lonely I was when I started university – being away from home for the first time and all that. DEV: Good idea. Everyone will be able to relate to that, although a lot of students were probably too embarrassed to admit to it. TAMARA: Yeah. We could discuss ways of dealing with loneliness as well, like just talking to strangers. DEV: Loads of studies have shown that interactions with shop assistants and bar staff make people feel more optimistic and relaxed. TAMARA: I don’t know about that, but it must make people feel more connected with their community. DEV: True, although you need to be a certain kind of person to be able to just strike up a conversation. TAMARA: Good point. We should say something about solitude and how being alone and being lonely aren’t the same thing. It’s strange the way some people can’t stand being by themselves while others love it. DEV: Yeah, the research shows a certain amount of solitude is beneficial for wellbeing, which I appreciate, but being alone isn’t something I actually like. I’d never choose to go on holiday alone, for example. TAMARA: Me neither. DEV: Well, let’s not… you' WHERE section_order = 3 AND test_id = @T2_ID;

UPDATE listening_section SET transcript = 'There are trends in food much as there are trends in clothing. Interest in food fashions has risen rapidly since the birth of the smartphone when people first began taking photos of their food and instantly sharing them with their friends. The food industry in the UK in particular is obsessed with finding and exploiting the next big food trend. Marketeers aim to create a huge demand for a food item which was previously unknown or not needed. One of the most effective ways of promoting a new food product is by using social media influencers as brand ambassadors. In return for free samples many influencers will post content about a product although there are influencers with hundreds of thousands of followers who can command large fees for their services. For a food item to become really popular, it has to be readily available. So supermarkets have a role to play in creating a new food trend. They have dedicated teams closely following which new products or ingredients are trending on social media and are particularly interested in what well-known chefs are putting on their menus. A British PR company was hired to raise its profile and stimulate demand. They paid for a group of journalists to travel out to South Africa to meet avocado farmers. Articles written following this visit helped to educate the British public about the avocado, which at this time was certainly not the daily staple it’s since become. Advertisements were designed to promote the avocado as a superfood, rich in nutrients and therefore beneficial for health. Avocados became hugely fashionable, and within a few years UK avocado sales had grown from £13 million annually to around £150 million, making it one of the most successful fresh produce campaigns in UK history. Oat milk is a recent example of a new product which became fashionable very quickly. Now there are many brands available but one company which had early success was the Swedish brand Oatly. They attracted a lot of attention with a media campaign which used provocation as a way of getting their message across effectively. The fact that this campaign aggravated competitors producing milk from dairy cows was seen as a plus, as it helped to make oat milk seem cool. In the USA, the brand decided against a big retail launch in favour of getting the product into coffee chains, which removed the need for a big advertising budget. This proved far more effective than offering samples in supermarkets. Oat milk had an advantage over other alternative milk products, such as almond milk. Many consumers prefer it because it has less of an impact on the environment. It requires significantly less water to produce than other alternative milk products and it also has a relatively low carbon footprint. Norwegian scray, a rarely available seasonal fish delicacy, otherwise known as Arctic cod, is now found on the menus of Michelin-starred restaurants throughout Europe. The demand for Skrei has been used by a food marketing agency to build the reputation of Norway’s fisheries in general. Marketing surveys have shown that a significant number of shoppers now associate Norway with excellent seafood. Food trends can be considered a good thing in some ways, as they can benefit farmers and food producers enormously. The public can also be encouraged to buy things which are more sustainably produced. But ethical concerns have been raised about the effects a surge in demand can cause. Quinoa is a classic example. This plant is native to Peru and when demand peaked some years ago the price soared making it unaffordable for local people. While the popularity of quinoa has benefited farmers financially there have been other negative consequences. As demand grew, farmers began working the land all year round in order to produce more quinoa. One issue has been that the fertility of the soil decreased dramatically, which could potentially lead to desertification in some areas.' WHERE section_order = 4 AND test_id = @T2_ID;

-- Assuming Test ID 3 was assigned to Cambridge 20 - Test 3
SET @T3_ID = 3;

UPDATE listening_section SET transcript = 'MAN: Good morning WOMAN: Hi, this is Michelin Meyer. I’m renting your house on Archwood Avenue. I’m due to move in next week. MAN: Oh, yes. Hello, Ms. Meyer. What can I do for you? WOMAN: When I viewed the house, I told you I’ll most probably need to rent some furniture, at least until I know whether my temporary work contract is going to be made permanent. MAN: Yes, of course. I remember. And I said I could give you some information about furniture rental companies in the city. WOMAN: That’s right. MAN: Well, the biggest company is called Peak Rentals. I’ve recommended them to other people and have always heard positive reports about them. WOMAN: Could you give me an idea of their costs? MAN: Sure. I actually have one of their brochures here. It says the monthly price per room starts at $105 and goes up to $239. That depends on which rooms you need furniture for, of course. WOMAN: Sure. It’s just to get a general idea of how much it’s going to cost. And you said you had some positive feedback about this company? MAN: Yes. People have mentioned that the furniture from Peak Rentals is more modern than any of the other companies. And also, once you place an order, the furniture will be delivered to you in just one or two days. WOMAN: That would be really helpful. MAN: Oh, and the brochure says that there’s a special offer at the moment. If you rent living room furniture, I believe that’s a set of chairs and a TV table, you’ll also get a lamp at no extra cost. WOMAN: Okay, but you know, that price range you gave is more than I was hoping to pay. MAN: Then you could try Aaron and Oliver. WOMAN: Sorry, what and Oliver? MAN: Aaron, double A-R-O-N WOMAN: OK, are they cheaper? MAN: I say they are a mid company. But if you chose them you need to be aware that they charge an extra 12 every month in case of damage WOMAN: Oh, I see. I’d have to do the math carefully then. MAN: Right. But one helpful thing is that they also do cleaning for customers. WOMAN: For the furniture? MAN: For the house. WOMAN: Oh, I see. I probably won’t need that. MAN: There’s another company called Larch Furniture. It’s quite new, and it has the lowest prices in town. That’s for both furniture and also electronic equipment. WOMAN: Well, that would be good. I’m not bringing much with me, and I won’t have much time to go shopping after I start my job. MAN: There are two things you need to know about large furniture. First of all you have to take out insurance on the furniture and you need to organize that yourself WOMAN: That wouldn’t be too hard. MAN: Also you can take out a contract for less than six months but I figure that might not be a problem for you. You’re renting the house for 12 months after all, aren’t you? WOMAN: Yes, okay. Well, I… MAN: Sorry to interrupt. I just thought of another furniture rental company. It’s called Space Rentals, and it’s located very near to the house. WOMAN: Okay. MAN: I don’t have any information about their charges, so it’s best to use their app to find out what it would cost you to use them. WOMAN: Okay, thanks. I’ll do that. MAN: One good thing about that company is that if you don’t like the furniture once it’s delivered, you can request exchanges, as long as you do that within a week of receiving it. WOMAN: That sounds really great. Okay, well, thanks very much. That’s so helpful.' WHERE section_order = 1 AND test_id = @T3_ID;

UPDATE listening_section SET transcript = 'Hello, I’m Hayden. I’m one of the archaeologists investigating the site here at Bidcuster. This is the third summer for this community project, and most of the people digging here are volunteers. I’m a full-time archaeologist for the town council, but I was asked to join the project by NHA, a charity which sets up projects like this up and down the country. As you can see, we’re next to Bidcaster Castle, which is great because the owners let us use their facilities. So, how did we get to where we are today? Many archaeology projects happen when an ancient object is found, and in our case that object was a gold coin. Coins are often found by people using metal detectors to look for things buried in the ground or coins are uncovered when wild animals like rabbits have been digging tunnels. Here, a walker found it on the ground after a rainstorm washed away some of the earth and sand. When the story of the gold coin hit the news, Peter Swift, an amateur historian, contacted me to say he believed there had been a village on this site, centuries before the castle was built. Just by chance, the team found some old maps and documents in our library, which showed 500-year-old drawings of ruined buildings on the grassy area between the outer stone walls of the castle and the river. We knew then we were onto something. Over the three summers the team has been here, we’ve found the remains of several buildings, and more broken pots than you can count. Normally you’d expect to find brooches and other jewellery, but we’re still waiting to uncover any such items. The people who once lived here were skilled at making tools from animal bones, as you’ll see when you visit the exhibition. Besides the discovery of the village, we’ve also found evidence of human activity on the other side of the river. No other houses or huts so far, but we can see the borders of an ancient field system. At one point we found a long wall and thought it was an ancient palace, but it turned out to be a modern wall. This summer’s work will end soon, but we’ll be back next summer. In the meantime, we’re putting on a series of guided tours for school groups this autumn. Oh, and maybe you saw the TV documentary about our project. That suggests the objects we’ve found are going to the town’s museum, but we don’t know that for sure yet. When you enter the site, please make sure you keep to the paths at all times. There are a few other things, the highlights of the site if you like, that I want to mention. Take a look at the map. Our present location is marked at the bottom. This year, we’ve identified the foundations of an ancient bridge, and it’s really exciting today because a team of divers are in the river searching for lost objects. To reach the bridge, take the main path ahead of you, go straight on, and keep going till the path bends to the left. You’ll see a smaller track leading off to the right. Follow that to take you to the river, where the divers are. You might be interested to see the rubbish pit. This is very near the castle walls in the north-west corner of the site. It actually dates to the time of the castle and not the ancient village. We found oyster shells and fish bones, and we assumed they were thrown from the castle kitchen above. One area we excavated in the first summer uncovered the site of a meeting hall. We knew it was an important building because it had two rows of post holes, deep enough to support a large roof. It is the largest structure in the central area of the site next to the current excavation area. Last year we discovered a fish pond in the ancient village. Normally these were beside a river. The pond here is further away, but it’s possible the river has moved slightly. Anyway, to get there from here, you turn right at the first information board you come to and follow the path into the trees. Before you come out of the trees, you’ll see it on your right. If you reach the river, you’ve gone too far. So, does anyone have…' WHERE section_order = 2 AND test_id = @T3_ID;

UPDATE listening_section SET transcript = 'MAYA: So, Finn, I’ve done as much as I can for our project on theatre programmes. How’s your research coming along? FINN: OK, Maya. I didn’t know theatre programmes are called playbills in the USA till I started looking into the topic. Even though I struggled to find many useful websites, I’m glad we picked this subject. No one else on the course is doing the same as us, although it is one of the research areas of the module convener. MAYA: That might actually put some people off. FINN: I suppose so. Anyway I hadn’t realised there are actually companies specialising in creating theatre programmes MAYA: Yes they’re quite common nowadays. Contrary to what many people think, theatres don’t hire people to do the programmes. In fact, companies buy the rights to publish programmes on the theatre’s behalf and then make their money selling advertising space within the programme booklet. FINN: It must be easier for theatres to do it that way. Yes. I remember reading something about programmes in early British theatre. It said that the cast was always very important. MAYA: Yeah, audiences were very familiar with leading actors and big names would draw huge crowds. FINN: But I hadn’t realised that if the programme named a famous actor, that’s who the public expected to perform. And if that didn’t happen, people accused the theatre of breaking their agreement with the audience. They would demand refunds and if they didn’t get them, there were riots. MAYA: Outrageous! That’d never happen now. FINN: No, people are too polite, even when they’re disappointed if the star of the show misses a performance. MAYA: We should definitely include that information about early audiences in our project. I also think it’s important to mention that lots of ordinary people at that time were illiterate, so theatre programmes were of limited value in advertising plays. When a company of actors arrived in a town, they’d parade around the streets in their costumes, beating drums and announcing their upcoming performances. FINN: Interesting. I couldn’t imagine that happening now either. MAYA: There’s also an interesting comparison to make between 18th and 19th century programmes. FINN: Wasn’t it in the 19th century that theatre programmes started to resemble programs today MAYA: Yes and unlike programs from the 18th century they always used colour. FINN: And there was a greater variety of designs. But personally, I think 18th century programmes were superior because they told the theatregoers so many things, including about the actors. MAYA: And about the writer, the plot and sometimes the history of the play. FINN: That’s right. What should we say about theatre programmes in the 20th century? MAYA: I reckon the most important thing is the dramatic change they underwent during World War II. FINN: When the government imposed restrictions on the use of paper. MAYA: Yeah, but that was only in the UK. In the USA, programmes, or rather playbills, continued to be published in the same format. FINN: While here in the UK, programmes became merely a single sheet of paper folded to create four pages for text. MAYA: What I don’t really get is that after the war they didn’t go back to being more than one sheet or change in any way for over 25 years. I know there were paper shortages after the war, but only for five or ten years. FINN: Strange. MAYA: I got some pictures of programmes we could include on the slides for our presentation. FINN: I found a couple too Maya. Let’s go through and see what we think. MAYA: Um oh this is an old one for a play called Ruey Blass. FINN: Never heard of that. But the programme looks very decorative. MAYA: Good enough to put in a frame on the wall. The images are just beautiful. FINN, what did you find? FINN: I’ve got some pages from a programme for Man of La Mancha. I thought this was a good programme to show, not because of the pictures, but because it contains articles written by members of the theatre company, so we can learn how the production was created and the thoughts and feelings of the cast. MAYA: Good. I’ve got a copy of a programme that’s now in a museum. It’s for The Tragedy of Jane Shore and it’s said to be the earliest surviving document to have been printed on Australia’s first printing press FINN: Fantastic! MAYA: Another programme to talk about is for The Sailors Festival. It comes from the British Library’s digitised collection of programmes that was started a few years ago. It already comprises over 200,000 programmes which is amazing! FINN: Huh. Wish I’d known about it while I was doing my research.' WHERE section_order = 3 AND test_id = @T3_ID;

UPDATE listening_section SET transcript = 'It’s only relatively recently that designers have become aware of the need to be inclusive when designing products. But what does that mean exactly? Well, it simply means designing products that span economic, social and cultural barriers. It means making sure products are accessible, so that as many different types of people as possible can use them, without any type of adaptation having to be made to the original design. Inclusive design is often linked with universal design, although they are not quite the same thing. Universal design aims to make products that work for everyone and that includes considering the needs of people who have cognitive difficulties, which can present quite a challenge. Today examples of successful inclusive design can be seen all around us. In workplaces it is common to see desks which can be adjusted to suit people of different heights or for wheelchair users This still isn’t always the case, however, and is one reason why office workers often suffer from back or neck problems. You’ll find another example in the public toilets of countless hotels, airports and offices. Taps that you activate by sensor require no pressing or twisting movements. These are not only more hygienic, they’re also easier for people with dexterity or mobility issues. The tech industry has been criticised in the past for focusing too much on young consumers, but this is changing. Many products are now designed with the elderly in mind. For example, it’s well known that vision declines with age and that we also become worse at distinguishing between similar colours, in particular shades of blue, which is why software designers rarely create interfaces with this colour. Motor skills also decline with age and some people have difficulty doing everyday things like picking up a cup or opening a door. This can also affect their ability to use a mouse or keyboard. So voice access is now a routine way of making commands. It’s worth looking at the problems non-inclusive designs cause when not enough consideration is given to a range of users, as it can have a serious impact on people’s lives. Access is one obvious example because it has such a huge impact on disabled people’s independence. Not being able to access public transport because buses or trains are not wheelchair accessible means many disabled people can go out unless someone goes with them. Safety is another issue. Inexplicably, most cars are still crash-tested using a dummy based on an average-sized male. This has safety implications for all women, particularly those who are pregnant, as the seatbelts worn by the dummy are not adapted to accommodate them. Over the past 100 years, workplaces in the UK have, on the whole, become considerably safer. Employers are legally required to provide well-maintained personal protective equipment, or PPE, anything from goggles to full bodysuits, to workers who need it, free of charge. But most PPE is designed to fit men. A recent report found that employers often think that when it comes to female workers, all they need to do to comply with this legal requirement is to buy jackets, for example, designed for a small man. The problem with this is that women can be tall and still have much smaller shoulders than the average man. Ill-fitting PPE such as high-vis jackets, vests and body armour can put women at risk. The report found that 95% of women said that their PPE often hampered their work and that this problem was worst in the emergency services. particularly the police. Another problem is related to comfort at work. A very common scenario in offices in summertime is to see women wrapped in blankets or wearing sweaters while the air conditioning is on high. Meanwhile, the men are in shorts and T-shirts. This is due to differences in metabolic rates for men and women. There is a standard setting for air conditioning to be at a temperature of 21 degrees designed to suit men and in most modern offices it is not possible to turn the air conditioning up or down This means that many offices which mainly employ women are wasting energy by having the air conditioning set too high. As you can see from the examples I’ve just mentioned, there are serious consequences for designs which don’t consider the needs of all users.' WHERE section_order = 4 AND test_id = @T3_ID;

-- Assuming Test ID 4 was assigned to Cambridge 20 - Test 4
SET @T4_ID = 4;

UPDATE listening_section SET transcript = 'MAN: Sandra, I seem to remember you had some family visitors staying with you recently. WOMAN: Yeah, that’s right. My brother and his family were here a couple of months ago. MAN: OK, good. Well, I wanted to ask for your advice. I got my cousin and her family visiting next month and as I don’t have kids, I’ve no idea where to take them. WOMAN: Right. What about accommodation? Are they going to stay with you in your flat? MAN: No, thankfully. There wouldn’t be room. My cousin wants me to recommend a hotel. Do you know anywhere? WOMAN: Yes, I do actually. I always recommend people stay at the King’s Hotel. MAN: Where’s that near? WOMAN: It’s about a five minutes walk from Murray Station, so nice and central. It’s actually on George Street. MAN: Oh yes, I know. WOMAN: I think they’re on quite a tight budget, so how much roughly is it to stay there? If you book a family room, it’s about £125 per night. My brother paid for two double rooms in the end, and I think that was around £95 for each room. MAN: Oh, that’s not too bad. WOMAN: So how old are your cousin’s kids? MAN: Twelve and nine. So I want to organise some trips while they’re here. I was thinking of doing a bus tour of the city centre, as none of them have been here before. WOMAN: Those bus tours are quite expensive. I think it’s better to do a walking tour. It gives you a much better feel for the city. There’s one that starts from Colton Square. It takes a couple of hours and doesn’t cost that much. MAN: Sounds good. I’ll look that up. Thanks. WOMAN: If the weather’s nice, one thing you could do is visit the old fort. You could get there by boat. The whole trip takes half a day. MAN: That’s a really good idea. I’d like to do that myself. And if the weather’s bad I was thinking they could go to the science museum. But maybe they could do that when I’m at work. WOMAN: Yeah, don’t forget it’s closed on Mondays. MAN: They’re here from Saturday for four nights so Tuesday would be best I think. WOMAN: And it won’t be so crowded then. Saturdays are terrible. I took my kids to the exhibition on old computers there and it was far too crowded. I wanted to go back but it’s finished now. MAN: That’s a shame. My cousin’s kids would have enjoyed that. WOMAN: There’s another one starting soon on space, which looks really good too. MAN: OK, well, I’ll mention that to my cousin. WOMAN: Have you thought about where to take them to eat? MAN: Well, I really like all the food stalls at Clacton Market. My cousin’s vegetarian. I know it’s one of the best places for that kind of food. WOMAN: Definitely, and there’ll be loads of choices for the kids too. You need to get there quite early, though. At the weekend, most of the stores stop serving lunch at 2.30. MAN: Good point. It’s all going to need careful planning. My cousin said she’d love to take the kids to a show at the theatre, but tickets are so expensive. WOMAN: I know. But you can get some good deals if you book online with bargained tickets for the following day. On some seats there is a 75% discount. MAN: Really? I must try and get some. WOMAN: Yeah. There are lots of things you can do for free as well. No need to spend a fortune. MAN: Like what? WOMAN: They’re coming next month, right? Well, check and see if it’s the same weekend as the Roots Music Festival in Blakewell Gardens. MAN: R-O-O-T-S? WOMAN: Yeah, check it out online. It’s always a family-friendly event and there’s no entry charge. MAN: That sounds perfect. WOMAN: And if you’re in Blakewell Gardens, climb Telegraph Hill. You’ll be able to look right down on the port. Everyone’s always really impressed because it’s so huge. MAN: Oh yeah, I’ve been meaning to do that for ages. I’ve heard the view’s amazing. WOMAN: Yeah, it’s really worth the effort. MAN: Well, that’s given me loads of ideas. Thanks so much.' WHERE section_order = 1 AND test_id = @T4_ID;

UPDATE listening_section SET transcript = 'Good morning and welcome to City Football Club. I’d like to give you some useful information about your visit to the stadium today and then we’ll start the tour of the areas of the stadium that are open to visitors. I can see lots of children here today, so just to let mums and dads know a few things before we start. The stadium has lots of stairs and the players’ tunnel is very dark. Please don’t let your children wander off on their own, even for a minute. We don’t want any accidents or anyone getting frightened. Cameras are permitted everywhere and you can take pictures of your child shooting a penalty. Assistants are helping to organise this and hopefully the queue won’t be too long. It’s very hot and sunny out on the pitch today. You can get food and drink at the cafe and I really recommend the healthy lunch boxes for children. Also in the cafe children are invited to do a football drawing. We pick the best one at the end of the afternoon so don’t forget to put your name and contact details on the back. That way if you’ve left the stadium before then, we’ll send your prize, but sadly we can’t return drawings. I’d like to mention some features of the tour. We’ll start with the 360 cinema experience, which has been very popular over the years, and then I’ll take you to the players’ dressing rooms, before going outside to the seating area and the pitch. I should say, if you’d prefer your visit to be self-guided, please collect headphones from the reception, and then you can listen to the pre-recorded information at your own speed. We’ve only just introduced this feature and would appreciate your feedback. We’re thinking of offering tours in other languages in future, so if you have any thoughts on that, we’d welcome those too. If you plan to return another time, you might like to book one of our VIP tours. We’ve only just started offering these and they can be booked online. Now, the stadium you see today was built in 1989 as part of a three-year redevelopment project. While that project was going on, the team had to play its matches at the ground of another club. Apart from that, the club has been here on this site since 1870. That was the start of a really important decade in the history of football in this country. For example, 1870 was also the year that football teams started to include a player whose role it was to guard the goal. In 1872 and 73, many other clubs were established, both here and abroad. In 1874, referees were allowed to send players off if they committed certain offences, and also in that year teams started having to swap ends at half-time. In early football games the aim was for the scorer to get the ball between two flag posts and later between sticks joined at the top with a piece of tape. In 1875, that tape was replaced with the solid crossbar that we’re familiar with today. In 1877, many clubs were founded and all agreed to set a 90-minute limit for each match. Before that, it was a more casual arrangement and this sometimes caused huge arguments and sometimes fights during matches. By 1878, the number of teams in the Football League increased again. Referees started using whistles and electric lamps were installed on certain pitches. This was a significant change, as games could then be played in the evenings all year round. In 1880, clubs began to charge fans for admission to games, even though players were still amateurs and had other jobs. That’s hard to imagine in the modern professional game, where top players earn significant sums of money from both playing and commercial activities.' WHERE section_order = 2 AND test_id = @T4_ID;

UPDATE listening_section SET transcript = 'MAN: How are you getting on with the assignment on handwriting? WOMAN: Not too bad. You know, I hadn’t realised that children benefit in so many ways from learning to write. It’s such an important skill, and yet most people think handwriting is less important than in the past, because people hardly ever write by hand these days. MAN: Yes, and all the evidence suggests children should learn to write by hand before they learn to type, not least because it helps their memory. WOMAN: That’s right. The physical act of writing helps children to remember letters. That seems pretty obvious when you think about it. MAN: What’s less obvious is how it helps develop their concentration. They have to sit still and focus on one thing. WOMAN: Yeah, that aspect of handwriting had never occurred to me before. MAN: Same here. I’m not sure I understand how it improves children’s imagination, though. WOMAN: Well there was that study which showed that primary age children generated more ideas when they were writing by hand than using a keyboard. I would have guessed that would be the case. MAN: Hmm yeah I never associated spatial awareness with handwriting either. I thought spatial awareness was more to do with knowing where you are in relation to objects or other people. WOMAN: I thought that too. But good spatial awareness is essential for writing because you have to space words correctly. It’s not just fine motor skills that improve through writing, as I’d always assumed. MAN: Handwriting is so much harder for children with dyspraxia, who have problems coordinating movement. It’s good there are lots of things you can do in the classroom to help them. They need so much more support with letter formation. You need to play lots of games to help them distinguish letter shapes. It takes a lot of patience. WOMAN: Yeah, I like the idea of using one of those pens that lights up if you press too hard. That seems like a really simple solution. MAN: Yes, absolutely. I’m not sure there’s much you can do about children with dyspraxia writing very slowly. It’s more important to focus on accuracy and as they get more confident, I think they eventually speed up. WOMAN: One quite simple thing you can do is to use grid paper. So they write each letter in a box and that trains them to space the letters correctly. MAN: Indeed, that’s more important for legibility than trying to get them to write in a straight line. MAN: For some children, it might be better to teach them to write on a laptop rather than by hand, like children with dyslexia. They often really struggle with handwriting and some just give up. WOMAN: Yeah, it’s not as frustrating for them if they get things wrong. On a keyboard, they can be more willing to have a go. But I read that developing fluency isn’t any faster. MAN: That’s right. Did you read that article on the benefits of teaching print rather than cursive handwriting, where the letters are joined up? WOMAN: Yes. Well, in the past, cursive writing was certainly considered more stylish and educated, but not anymore. Teachers’ attitudes have changed because it’s been proved that cursive is more difficult to learn, especially for children with learning difficulties who find joining up letters really challenging. MAN: I agree. I was always worried that my poor handwriting affected my exam results, and now research shows that I was right to worry. I’m sure a lot of students think it’s unfair that they’re being judged on their handwriting, not just their knowledge. WOMAN: Marks are definitely affected if examiners can’t read the script. That is why it has always been so important to teach children to write legibly. Do you think the role of handwriting will change in the future? MAN: I can’t see that changing much. Touch typing still isn’t taught in most schools, which is a shame. But maybe that won’t be necessary in the future, because people will also be able to write by hand on digital devices. Anyway, teachers understand the value of handwriting. It’s a basic life skill. WOMAN: True. However, the fact is that people are writing by hand less and less and relying on digital devices. That does cause some problems. MAN: You mean like note-taking. There are lots of apps for that. WOMAN: And for reading historical documents, apparently. But my mum is shocked by my awful spelling and the fact that my punctuation is really inconsistent. I think you can put that down to lack of practice. MAN: I expect so. Personally, I miss writing by hand. I hardly ever write anything now. I remember my grandparents had such beautiful handwriting and it was so individual. Nobody I know would be able to identify my handwriting now. It’s a shame. WOMAN: I know. I feel the same way. I used to write a diary by hand and now I do that digitally. It just seems less effort to do it that way. So it’s not just a problem…' WHERE section_order = 3 AND test_id = @T4_ID;

UPDATE listening_section SET transcript = 'We’ve been looking at different types of conflicts that may arise between wildlife and humans at the boundaries of protected areas, such as national parks and animal sanctuaries. I’d like to illustrate this by telling you about some research that I’ve been involved in recently in the Central African country of Zambia in the area around the Chembe Bird Sanctuary which contains over 300 of the listed birds of Zambia. These include a number of birds of prey such as eagles, hawks and owls that live by hunting and killing other birds and animals. Now most of the people living in the local communities near to the bird sanctuary are small-scale farmers and these birds of prey provide important social and ecological benefits to them. For example, a lot of damage can be caused to farmers’ crops by rodents, such as rats, which would consume the crops as they grow in the fields, as well as after harvesting if they weren’t hunted and killed by the birds. And the predatory habits of these birds also protect farmers in other ways. For example, a major danger to rural workers is snakes, whose bite may be dangerous or even fatal, and birds of prey have a major role in keeping their populations under control. Local people have always been aware of these benefits and for years, even before the sanctuary was opened in 1973, the birds played a key role in the culture of the region. However, more recently, the sanctuary and its birds have also become increasingly important to the community in economic terms, because at present, after a relatively slow start, tourism has become an important source of revenue for them. However, although these birds of prey are protected by the government, their numbers are falling. Some of these deaths are accidental. Fatalities occur when birds alight on roads to catch and eat their prey, and are hit by fast moving traffic. Drivers in Zambia have to take special care at night, as birds may regard the quieter roads as safe places to sleep. Accidental deaths may also occur if these birds fly close to high power lines as they may be electrocuted. This is a particular danger in the heavy rain which can occur in the region in the months from December to April. And local farmers also pose a threat to these birds. As well as growing crops, small-scale farmers in the area also rear chickens. These provide food for the farmers’ families, as well as being an important source of income. But they’re also an easy target for birds of prey, and so farmers may shoot these birds, which is illegal but understandable, or they may poison the birds, which again is illegal and can have negative effects on the ecosystem. So how else can farmers protect their chickens from birds of prey? Some people believe that to prevent the predators from settling near the area where the chickens are kept, it’s best to keep this area free from vegetation. But in fact, this is counterproductive, as it means the chickens have no cover to hide in and they’ll be easier for the birds to see. Another possibility would be to prevent the chickens from going outside at all and to keep them safe from predators inside a building, but this would cost far too much to be a practical solution. Nearly all the farmers reported that they spent a lot of time and effort trying to frighten off the birds of prey without actually harming them. Most of the farmers had at least one dog and said this was a big help at scaring away the predators. Some of the farmers also reported that during the breeding season, when the chickens were particularly vulnerable, they encouraged their children to watch over the chickens and to hit pans with a metal spoon so that the resulting noise would succeed in driving away birds that were trying to seize the young chicks. None of these methods was 100% effective so as a result the village people told us that rather than just using one method, they were forced to use a combination for them to have any effect. And even so, these birds of prey remain a major threat to the chickens’ survival and cause considerable economic loss to farmers. So we looked at the possibility of a longer-term solution to…' WHERE section_order = 4 AND test_id = @T4_ID;

-- Assuming Test ID 5 was assigned to Cambridge 19 - Test 1
SET @T5_ID = 5;

UPDATE listening_section SET transcript = 'Sally: Welcome to Hinchingbrooke Country Park. I’m Sally, and I’ll tell you more about what you can discover here today. John: Thanks! It’s my first visit—how big is this park? Sally: The park covers 69 hectares, including woodland, meadows, and a wetland zone. John: What’s in the wetland area? Sally: Well, the wetland was created by flooding old farmland. You’ll see two large lakes, several ponds, and a stream running through it. John: That’s lovely. I’ve got a school group coming. Are there any educational activities? Sally: Yes, absolutely. In science, children can collect data on plants, trees, and insects—record and analyze what they observe. In geography, they can learn to use a map and compass to navigate around the park. Leisure and tourism sessions focus on visitors, exploring ways people enjoy nature and the park’s facilities. In music workshops, kids can make sounds using natural materials—drumming on logs or rattles with seeds, for instance. John: That sounds great—I think outdoor learning is important. Sally: It is — children get a sense of freedom that they might not experience otherwise. They can also develop new skills, like teamwork, observation, and creative thinking. John: Practical question—how much does it cost? Sally: If your group has more than 30 children, it’s £4.95 per child for a full-day visit. John: And for adults joining? Sally: There’s no charge for leaders and any accompanying adults.' WHERE section_order = 1 AND test_id = @T5_ID;

UPDATE listening_section SET transcript = 'It’s great to see so many members of the Twinning Association here tonight. Since the twinning link between our two towns, Stanthorpe here in England and Malatte in France, was established, the relationship between the towns has gone from strength to strength. Last month, 25 members of the association from Stanthorpe spent a weekend in Malatte. Our hosts had arranged a great programme. We learned how cheese is produced in the region and had the chance to taste the products. The theme park trip had to be cancelled, but we all had a great time on the final boat trip down the river – that was the real highlight. This is a special year for the Association because it’s 25 years since we were founded. In Malatte, they’re planning to mark this by building a footbridge in the municipal park. We’ve been discussing what to do here and we’ve decided to plant a poplar tree in the museum gardens. We considered buying a garden seat to put there, but the authorities weren’t happy with that idea. In terms of fundraising to support our activities, we’ve done very well. Our pancake evening was well attended and made record profits. And everyone enjoyed the demonstration of French cookery, which was nearly as successful. Numbers for our film show were limited because of the venue so we’re looking for somewhere bigger next year. We’re looking forward to welcoming our French visitors here next week, and I know that many of you here will be hosting individuals or families. The coach from France will arrive at 5 pm on Friday. Don’t try to do too much that first evening as they’ll be tired, so have dinner in the house or garden rather than eating out. The weather looks as if it’ll be OK so you might like to plan a barbecue. Then the next morning’s market day in town, and that’s always a good place to stroll round. On Saturday evening, we’ll all meet up at the football club, where once again we’ll have Toby Sharp and his band performing English and Scottish country songs.' WHERE section_order = 2 AND test_id = @T5_ID;

UPDATE listening_section SET transcript = 'COLIN: I was reading an article about food trends predicting how eating habits might change in the next few years. MARIE: Oh – things like more focus on local products? That seems so obvious, but the shops are still full of imported foods. COLIN: Yes, they need to be more proactive to address that. MARIE: And somehow motivate consumers to change, yes. COLIN: One thing everyone’s aware of is the need for a reduction in unnecessary packaging – but just about everything you buy in supermarkets is still covered in plastic. The government needs to do something about it. MARIE: Absolutely. It’s got to change. COLIN: Do you think there’ll be more interest in gluten- and lactose-free food? MARIE: For people with allergies or food intolerances? I don’t know. Lots of people I know have been buying that type of food for years now. COLIN: Yes, even if they haven’t been diagnosed with an allergy. MARIE: That’s right. One thing I’ve noticed is the number of branded products related to celebrity chefs – people watch them cooking on TV and then buy things like spice mixes or frozen foods with the chef’s name on… I bought something like that once, but I won’t again. COLIN: Yeah – I bought a ready-made spice mix for chicken which was supposed to be used by a chef I’d seen on television, and it didn’t actually taste of anything. MARIE: Mm. Did the article mention ‘ghost kitchens’ used to produce takeaway food? COLIN: No. What are they? MARIE: Well, they might have the name of a restaurant, but actually they’re a cooking facility just for delivery meals – the public don’t ever go there. But people aren’t aware of that – it’s all kept very quiet. COLIN: So people don’t realise the food’s not actually from the restaurant? MARIE: Right. COLIN: Did you know more and more people are using all sorts of different mushrooms now, to treat different health concerns? Things like heart problems? MARIE: Hmm. They might be taking a big risk there. COLIN: Yes, it’s hard to know which varieties are safe to eat. MARIE: Anyway, maybe now…' WHERE section_order = 3 AND test_id = @T5_ID;

UPDATE listening_section SET transcript = 'For my presentation today, I’m going to talk about the Céide Fields in the northwest of Ireland, one of the largest Neolithic sites in the world. I recently visited this site and observed the work that is currently being done by a team of archaeologists there. The site was first discovered in the 1930s by a local teacher, Patrick Caulfield. He noticed that when local people were digging in the bog, they were constantly hitting against what seemed to be rows of stones. He realised that these must be walls and that they must be thousands of years old for them to predate the bog which subsequently grew over them. He wrote to the National Museum in Dublin to ask them to investigate, but no one took him seriously. It wasn’t until 40 years later, when Patrick Caulfield’s son Seamus, who had become an archaeologist by then, began to explore further. He inserted iron probes into the bog to map the formation of the stones, a traditional method which local people had always used for finding fuel buried in the bog for thousands of years. Carbon dating later proved that the site was over 5,000 years old and was the largest Neolithic site in Ireland. Thanks to the bog which covers the area, the remains of the settlement at Céide Fields, which is over 5,000 years old, are extremely well-preserved. A bog is 90 percent water; its soil is so saturated that when the grasses and heathers that grow on its surface die, they don’t fully decay but accumulate in layers. Objects remain so well preserved in these conditions because of the acidity of the peat and the deficiency of oxygen. At least 175 days of rain a year are required for this to happen; this part of Ireland gets an average of 225 days. The Neolithic farmers at Céide would have enjoyed several centuries of relative peace and stability. Neolithic farmers generally lived in larger communities than their predecessors, with a number of houses built around a community building. As they lived in permanent settlements, Neolithic farmers were able to build bigger houses. These weren’t round as people often assume, but rectangular with a small hole in the roof that allowed smoke to escape. This is one of many innovations and indicates that the Neolithic farmers were the first people to cook indoors. Another new technology that Neolithic settlers brought to Ireland was pottery. Fragments of Neolithic pots have been found in Céide and elsewhere in Ireland. The pots were used for many things; as well as for storing food, pots were filled with a small amount of fat and when this was set alight, they served as lamps. It’s thought that the Céide Fields were mainly used as paddocks for animals to graze in. Evidence from the Céide Fields suggests that each plot of land was of a suitable size to sustain an extended family. They may have used a system of rotational grazing in order to prevent over-grazing and to allow for plant recovery and regrowth. This must have been a year-round activity as no structures have been found which would have been used to shelter animals in the winter. However, archaeologists believe that this way of life at Céide ceased abruptly. Why was this? Well, several factors may have contributed to the changing circumstances. The soil would have become less productive and led to the abandonment of farming. The crop rotation system was partly responsible for this as it would have been very intensive and was not sustainable. But there were also climatic pressures too. The farmers at Céide would have enjoyed a relatively dry period, but this began to change and the conditions became wetter as there was a lot more rain. It was these conditions that encouraged the bog to form over the area which survives today.' WHERE section_order = 4 AND test_id = @T5_ID;

-- Assuming Test ID 6 was assigned to Cambridge 19 - Test 2
SET @T6_ID = 6;

UPDATE listening_section SET transcript = 'WOMAN: Hi Coleman, how are you? COLEMAN: Good, thanks. WOMAN: I wanted to have a chat with you because our friend Josh told me that you’ve joined a guitar group and it sounds interesting. I’d really like to learn myself. COLEMAN: Why don’t you come along? I’m sure there’s room for another person. WOMAN: Really? So – who runs the classes? COLEMAN: He’s called a ‘coordinator’ – his name’s Gary Mathieson. WOMAN: Let me note that down. Gary… How do you spell his surname? COLEMAN: It’s M-A-T-H-I-E-S-O-N WOMAN: Right, thanks. COLEMAN: He’s retired, actually, but he’s a really nice guy and he used to play in a lot of bands. WOMAN: Thanks. So how long have you been going? COLEMAN: About a month now. WOMAN: And could you play anything before you started? COLEMAN: I knew a few chords, but that’s all. WOMAN: I’m sure everyone will be better than me. COLEMAN: That’s what I thought, too. When I first spoke to Gary on the phone, he said it was a class for beginners, but I was still worried that everyone would be better than me, but we were all equally hopeless! WOMAN: That’s reassuring. So where do you meet? COLEMAN: It used to be at Lock Street in the city centre. WOMAN: Oh, I know where that is. COLEMAN: Yeah, they were meeting in Gary’s home, but as the group got bigger, he decided to book a room at the college in town. I prefer going there. WOMAN: The college on New Street? COLEMAN: It’s just beyond there at the bottom of New Street near the city roundabout. WOMAN: OK. And what time is the class? COLEMAN: It used to be 10.30 and that suited me well, but now we meet at 11. WOMAN: On Thursday morning. COLEMAN: Yeah. WOMAN: And do I need to buy a guitar? COLEMAN: I bought a second-hand one. There’s a website called ‘The perfect instrument’ that sells all kinds of guitars – some new ones, but others that have been hardly used. WOMAN: Oh, that sounds good. So, how does the lesson run? COLEMAN: He always starts by getting us to tune our guitars. That takes about five minutes. WOMAN: How do you do that? COLEMAN: Some people use an app, but Gary shows us how to tune by ear, too. WOMAN: And then? COLEMAN: We strum chords using our thumbs – it sounds so much better when there are six of you playing together! Sometimes, when we’re off-beat, Gary starts clapping to help us. WOMAN: Right. COLEMAN: Then, for the last 15 minutes, we play songs. If there’s a new chord in it, we practise that before we play it together – but really slowly. WOMAN: That sounds great. COLEMAN: He gets us to play single notes and simple tunes too. WOMAN: That’s good. COLEMAN: The only trouble is that he sometimes gets us to play one at a time – you know, alone. I find that quite intimidating. WOMAN: I can imagine. Well, thanks for the info, Coleman. I think I’m going to sign up.' WHERE section_order = 1 AND test_id = @T6_ID;

UPDATE listening_section SET transcript = 'I never really planned to be a lifeboat volunteer when I came to live in Northsea. I’d been working in London as a website designer, but although that was interesting, I didn’t like city life. I’d been really keen on boats as a teenager, and I thought if I went to live by the sea, I might be able to pursue that interest a bit more in my free time. Then I found that the Lifeboat Institution was looking for volunteers, so I decided to apply. The Lifeboat Institution building here in Northsea’s hard to miss; it’s one of the largest in the country. It was built 15 years ago with funds provided by a generous member of the public, who’d lived here all her life. As the Lifeboat Institution is a charity that relies on that kind of donation, rather than funding provided by the government, that kind of help is much needed. When I applied, I had to have a health assessment. The doctors were particularly interested in my vision. I used to be short-sighted, so I’d had to wear glasses, but I’d had laser eye surgery two years earlier so that was OK. They gave me tests for colour blindness and they thought I might have a problem there, but it turned out I was OK. When the coastguard gets an alert, all the volunteers are contacted and rush to the lifeboat station. Our target’s to get there in five minutes, then we try to get the boat off the dock and out to sea in another six to eight minutes. Our team’s proud that we usually achieve that – the average time across the country’s eight and a half minutes. As well as steering the lifeboat, as a helmsman, I have the ultimate responsibility for the lifeboat. I have to check that the equipment we use is in working order – we have special life jackets that can support up to four people in the water. And it’s ultimately my decision whether it’s safe to launch the boat. But it’s very rare not to launch, even in the worst weather. A lot of people underestimate how windy conditions can change at sea, so I speak to youth groups and sailing clubs in the area about the sorts of problems that sailors and swimmers can have if the weather suddenly gets bad. We also have a lot of volunteers who organise activities to raise money for us, and we couldn’t manage without them. The training we get is a continuous process, focusing on technical competence and safe handling techniques, and it’s given me the confidence to deal with extreme situations without panicking. I was glad I’d done a first aid course before I started, as that’s a big help with the casualty care activities we do. We’ve done a lot on how to deal with ropes and tie knots – that’s an essential skill. After a year, I did a one-week residential course, led by specialists. They had a wave-tank where they could create extreme weather conditions – so we could get experience at what to do if the boat turned over in a storm at night, for example. Since I started, I’ve had to deal with a range of emergency situations. But the work’s hugely motivating. It’s not just about saving lives – I’ve learned a lot about the technology involved. My background in IT’s been useful here, and I can use my expertise to help other volunteers. They’re a great group – we’re like a family really, which helps when you’re dragging yourself out of bed on a cold stormy night. But actually, it’s the colder months that can be the most rewarding time. That’s when the incidents tend to be more serious, and you realise that you can make a huge difference to the outcome.' WHERE section_order = 2 AND test_id = @T6_ID;

UPDATE listening_section SET transcript = 'BELLA: Hi Don – did you get the copy of the article on recycling footwear that I emailed you? DON: Yeah – it’s here … I’ve had a look at it. BELLA: So do you think it’s a good topic for our presentation? DON: Well, before I started reading it, I thought recycling footwear, well, although it’s quite interesting, perhaps there isn’t enough to say about it, cos we put shoes in recycling bins, they go to charity shops and that’s about it. BELLA: … but there’s much more to it than that. DON: I realise that now and I’m keen to research the topic more. BELLA: That’s great. DON: One of the things I didn’t realise until I read the article was just how many pairs of trainers get recycled! BELLA: Well, a lot of young people wear them all the time now. They’ve become more popular than ordinary shoes. DON: I know. I guess they are very hard-wearing, but don’t they look a bit casual for school uniform? I don’t think they’re right for that. BELLA: Actually, I think some of them look quite smart on pupils … better than a scruffy old pair of shoes. DON: So do you keep shoes a long time? BELLA: Yes. Though I do tend to wear my old pairs for doing dirty jobs like cleaning my bike. BELLA: I must admit, I’ve recycled some perfectly good shoes, that haven’t gone out of fashion and still fit, just because they don’t look great on me any more. That’s awful isn’t it? DON: I think it’s common because there’s so much choice. The article did say that recent sales of footwear have increased enormously. BELLA: That didn’t surprise me. DON: No. But then it said that the amount of recycled footwear has fallen: it’s 6 percent now compared to a previous level of 11 percent. That doesn’t seem to make sense. BELLA: That’s because not everything goes through the recycling process. Some footwear just isn’t good enough to re-sell, for one reason or another, and gets rejected. BELLA: So let’s find some examples in the article of footwear that was rejected for recycling. DON: OK. I think there are some in the interview with the recycling manager. Yeah – here it is. BELLA: Mmm. Let’s start with the ladies’ high-heeled shoes. What did he say about those? DON: He said they were probably expensive – the material was suede and they were beige in colour – it looked like someone had only worn them once, but in a very wet field so the heels were too stained with mud and grass to re-sell them. BELLA: OK … and the leather ankle boots. What was wrong with them? DON: Apparently, the heels were worn – but that wasn’t the problem. One of the shoes was a much lighter shade than the other one – it had obviously been left in the sun. I suppose even second-hand shoes should look the same! BELLA: Sure. Then there were the red baby shoes. DON: Oh yes – we’re told to tie shoes together when we put them in a recycling bin, but people often don’t bother. BELLA: You’d think it would have been easy to find the other, but it wasn’t. That was a shame because they were obviously new. DON: The trainers were interesting. He said they looked like they’d been worn by a marathon runner. BELLA: Yeah – weren’t they split? DON: Not exactly. One of the soles was so worn under the foot that you could put your finger through it. BELLA: Well, we could certainly use some of those examples in our presentation to explain why 90 percent of shoes that people take to recycling centres or bins get thrown into landfill. DON: Mmm. What did you think about the project his team set up to avoid this by making new shoes out of the good parts of old shoes? BELLA: It sounded like a good idea. They get so many shoes, they should be able to match parts. I wasn’t surprised that it failed, though. I mean who wants to buy second-hand shoes really? Think of all the germs you could catch! DON: Well, people didn’t refuse them for that reason, did they? It was because the pairs of shoes weren’t identical. BELLA: They still managed to ship them overseas, though. DON: That’s another area we need to discuss. BELLA: You know I used to consider this topic just from my own perspective, by thinking about my own recycling behaviour without looking at the bigger picture. So much happens once shoes leave the recycling area. DON: It’s not as simple as you first think, and we can show that by taking a very different approach to it. BELLA: Absolutely. So let’s discuss …' WHERE section_order = 3 AND test_id = @T6_ID;

UPDATE listening_section SET transcript = 'For my project on invertebrates, I chose to study tardigrades. These are microscopic — or to be more precise — near-microscopic animals. There are well over a thousand known species of these tiny animals, which belong to the phylum Tardigrada. Most tardigrades range in length from 0.05 to 1 millimetre, though the largest species can grow to be 1.2 millimetres in length. They are also sometimes called ‘water bears’: ‘water’ because that’s where they thrive best, and ‘bear’ because of the way they move. ‘Moss piglet’ is another name for tardigrades because of the way they look when viewed from the front. They were first discovered in Germany in 1773 by Johann Goeze, who coined the name Tardigrada. As I say, there are many different species of tardigrade — too many to describe here — but, generally speaking, the different species share similar physical traits. They have a body which is short, and also rounded — a bit like a barrel — and the body comprises four segments. Each segment has a pair of legs, at the end of which are between four and eight sharp claws. I should also say that some species don’t have any claws: what they have are discs, and these work by means of suction. They enable the tardigrade to cling to surfaces or to grip its prey. Within the body, there are no lungs, or any organs for breathing at all. Instead, oxygen and also blood are transported in a fluid that fills the cavity of the body. As far as the tardigrade’s head is concerned, the best way I can describe this is that it looks rather strange — a bit squashed even — though many of the websites I looked at described its appearance as cute, which isn’t exactly very scientific. The tardigrade’s mouth is a kind of tube that can open outwards to reveal teeth-like structures known as ‘stylets’. These are sharp enough to pierce plant or animal cells. So, where are tardigrades found? Well, they live in every part of the world, in a variety of habitats: most commonly, on the bed of a lake, or on many kinds of plants or in very wet environments. There’s been some interesting research which has found that tardigrades are capable of surviving radiation and very high pressure, and they’re also able to withstand temperatures as low as minus –200 degrees centigrade, or highs of more than 148 degrees centigrade, which is incredibly hot. It has been said that tardigrades could survive long after human beings have been wiped out, even in the event of an asteroid hitting the earth. If conditions become too extreme and tardigrades are at risk of drying out, they enter a state called cryptobiosis. They form a little ball, called a tun — that’s T-U-N — by retracting their head and legs, and their metabolism drops to less than one percent of normal levels. They can stay in this state for decades, and if re-introduced to water, when they will come back to life in a matter of a few hours. While in this state of cryptobiosis, tardigrades produce a protein that protects their DNA. In 2016, scientists revived two tardigrades that had been tuns for more than 30 years. There was a report that,in 1948, a 120-year-old tun was revived, but this experiment has never been repeated. There are currently several tests taking place in space, to determine how long tardigrades might be able to survive there. I believe the record so far is 10 day. So, erm, moving on. In terms of their diet, tardigrades consume liquids in order to survive. Although they have teeth, they don’t use these for chewing. They suck the juices from moss, or extract fluid from seaweed, but some species prey on other tardigrades, from other species or within their own. I suppose this isn’t surprising, given that tardigrades are mainly comprised of liquid and are coated with a type of gel. Finally, I’d like to mention the conservation status of tardigrades. It is estimated that they have been in existence for approximately half a billion years and, in that time, they have survived five mass extinctions. So, it will probably come as no surprise to you, that tardigrades have not been evaluated by the International Union for Conservation of Nature and are not on any endangered list. Some researchers have described them as thriving. Does anyone have any questions they’d like to ask?' WHERE section_order = 4 AND test_id = @T6_ID;

COMMIT;
SET foreign_key_checks=1;
SET unique_checks=1;
SET autocommit=1;