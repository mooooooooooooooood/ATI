-- Use or create database
CREATE DATABASE IF NOT EXISTS ielts_db;
USE ielts_db;

-- 1. Disable foreign key checks for clean drop
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Drop ALL Tables (including the ones we are now changing/removing)
DROP TABLE IF EXISTS reading_user_answer;
DROP TABLE IF EXISTS reading_question;
DROP TABLE IF EXISTS reading_question_group;
DROP TABLE IF EXISTS reading_passage;
DROP TABLE IF EXISTS reading_question_type;
DROP TABLE IF EXISTS reading_test;

-- 3. Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

---
## 📝 Table Creation in Correct Order

-- 1️⃣ Table: reading_test (Parent of reading_passage)
CREATE TABLE reading_test (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    test_name VARCHAR(255) NOT NULL,
    test_level ENUM('Academic', 'General') DEFAULT 'Academic',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2️⃣ Table: reading_question_type (Parent of reading_question)
CREATE TABLE reading_question_type (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL
);

-- Insert default IELTS reading question types
INSERT INTO reading_question_type (type_name) VALUES
('True/False/Not Given'),
('Yes/No/Not Given'),
('Multiple Choice'),
('Matching Headings'),
('Matching Information'),
('Matching Features'),
('Sentence Completion'),
('Summary Completion'),
('Table Completion'),
('Diagram Label Completion'),
('Short Answer Questions');

-- 3️⃣ Table: reading_passage (Child of reading_test)
CREATE TABLE reading_passage (
    passage_id INT AUTO_INCREMENT PRIMARY KEY,
    test_id INT NOT NULL,
    title VARCHAR(255),
    passage_text TEXT,
    passage_order INT DEFAULT 1,
    FOREIGN KEY (test_id) REFERENCES reading_test(test_id) ON DELETE CASCADE
);

-- 4️⃣ Table: reading_question_group (Child of reading_passage)
CREATE TABLE reading_question_group (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    passage_id INT NOT NULL,
    instructions TEXT NOT NULL,
    group_order INT DEFAULT 1,
    FOREIGN KEY (passage_id) REFERENCES reading_passage(passage_id) ON DELETE CASCADE
);

-- 5️⃣ Table: reading_question (Child of question_group and type)
CREATE TABLE reading_question (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    type_id INT NOT NULL,
    question_text TEXT NOT NULL,
    options JSON NULL,
    correct_answer VARCHAR(255),
    question_order INT DEFAULT 1,
    FOREIGN KEY (group_id) REFERENCES reading_question_group(group_id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES reading_question_type(type_id)
);

-- 6️⃣ Table: reading_user_answer (MODIFIED: user_id is NOT an F.K. and is NULLABLE)
CREATE TABLE reading_user_answer (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL, -- CHANGED to NULL to allow answers without a user
    question_id INT NOT NULL,
    user_response VARCHAR(255),
    is_correct BOOLEAN,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES reading_question(question_id) ON DELETE CASCADE
    -- The FOREIGN KEY (user_id) to the users table is REMOVED
);

-- 1. Insert the Test record and capture its ID
INSERT INTO reading_test (test_name, test_level) 
VALUES ('Sample IELTS Reading Test 1 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 📚 PART 2: PASSAGE 1: The kākāpō (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The kākāpō',
    'The kākāpō is a nocturnal, flightless parrot that is critically endangered and one of New Nealand\'s unique treasures\nThe kākāpō, also known as the owl parrot, is a large, forest-dwelling bird, with a pale owl-like face. Up to 64 cm in length, it has predominantly yellow-green feathers, forward-facing eyes, a large grey beak, large blue feet, and relatively short wings and tail. It is the world\'s only flightless parrot, and is also possibly one of the world\'s longest-living birds, with a reported lifespan of up to 100 years. \n\nKākāpō are solitary birds and tend to occupy the same home range for many years. They forage on the ground and climb high into trees. They often leap from trees and flap their wings, but at best manage a controlled descent to the ground. They are entirely vegetarian, with their diet including the leaves, roots and bark of trees as well as bulbs, and fern fronds.\n\nKākāpō breed in summer and autumn, but only in years when food is plentiful. Males play no part in incubation or chick-rearing - females alone incubate eggs and feed the chicks. The 1-4 eggs are laid in soil, which is repeatedly turned over before and during incubation. The female kākāpō has to spend long periods away from the nest searching for food, which leaves the unattended eggs and chicks particularly vulnerable to predators.\n\nBefore humans arrived, kākāpō were common throughout New Zealand\'s forests. However, this all changed with the arrival of the first Polynesian settlers about 700 years ago. For the early settlers, the flightless kākāpō was easy prey. They ate its meat and used its feathers to make soft cloaks. With them came the Polynesian dog and rat, which also preyed on kākāpō. By the time European colonisers arrived in the early 1800s, kākāpō had become confined to the central North Island and forested parts of the South Island. The fall in kākāpō numbers was accelerated by European colonisation. A great deal of habitat was lost through forest clearance, and introduced species such as deer depleted the remaining forests of food. Other predators such as cats, stoats and two more species of rat were also introduced. The kākāpō were in serious trouble.\n\nIn 1894, the New Zealand government launched its first attempt to save the kākāpō. Conservationist Richard Henry led an effort to relocate several hundred of the birds to predator-free Resolution Island in Fiordland. Unfortunately, the island didn\'t remain predator free - stoats arrived within six years, eventually destroying the kākāpō population. By the mid-1900s, the kākāpō was practically a lost species. Only a few clung to life in the most isolated parts of New Zealand.\n\nFrom 1949 to 1973, the newly formed New Zealand Wildlife Service made over 60 expeditions to find kākāpō, focusing mainly on Fiordland. Six were caught, but there were no females amongst them and all but one died within a few months of captivity. In 1974, a new initiative was launched, and by 1977, 18 more kākāpō were found in Fiordland. However, there were still no females. In 1977, a large population of males was spotted in Rakiura - a large island free from stoats, ferrets and weasels. There were about 200 individuals, and in 1980 it was confirmed females were also present. These birds have been the foundation\nof all subsequent work in managing the species. \n\nUnfortunately, predation by feral cats on Rakiura Island led to a rapid decline in kākāpō numbers. As a result, during 1980-97, the surviving population was evacuated to three island sanctuaries: Codfish Island, Maud Island and Little Barrier Island. However, breeding success was hard to achieve. Rats were found to be a major predator of kākāpō chicks and an insufficient number of chicks survived to offset adult mortality. By 1995, although at least 12 chicks had been produced on the islands, only three had survived. The kākāpō population had dropped to 51 birds. The critical situation prompted an urgent review of\nkākāpō management in New Zealand.\n\nIn 1996, a new Recovery Plan was launched, together with a specialist advisory group called the Kākāpō Scientific and Technical Advisory Committee and a higher amount of funding. Renewed steps were taken to control predators on the three islands. Cats were eradicated from Little Barrier Island in 1980, and possums were eradicated from Codfish Island by 1986. However, the population did not start to increase until rats were removed from all three islands, and the birds were more intensively managed. This involved moving the birds between islands, supplementary feeding of adults and rescuing and hand-raising any failing\nchicks.\n\nAfter the first five years of the Recovery Plan, the population was on target. By 2000, five new females had been produced, and the total population had grown to 62 birds. For the first time, there was cautious optimism for the future of kākāpō and by June 2020, a total of 210 birds was recorded. \n\nToday, kākāpō management continues to be guided by the kākāpō Recovery Plan. Its key goals are: minimise the loss of genetic diversity in the kākāpō population, restore or maintain sufficient habitat to accommodate the expected increase in the kākāpō population, and ensure stakeholders continue to be fully engaged in the preservation of the species.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage1_id, 'Questions 1-6: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 1, 'There are other parrots that share the kakapo\'s inability to fly.', 'FALSE', 1),
(@group1_id, 1, 'Adult kakapo produce chicks every year.', 'FALSE', 2),
(@group1_id, 1, 'Adult male kakapo bring food back to nesting females.', 'FALSE', 3),
(@group1_id, 1, 'The Polynesian rat was a greater threat to the kakapo than Polynesian settlers.', 'NOT GIVEN', 4),
(@group1_id, 1, 'Kakapo were transferred from Rakiura Island to other locations because they were at risk from feral cats.', 'TRUE', 5),
(@group1_id, 1, 'One Recovery Plan initiative that helped increase the kakapo population size was caring for struggling young birds.', 'TRUE', 6);

-- C. Group 2 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage1_id, 'Questions 7-13: Complete the notes below. Choose ONE WORD AND/OR A NUMBER from the passage for each answer.', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 8, 'diet consists of fern fronds, various parts of a tree and 7_________', 'bulbs', 7),
(@group2_id, 8, 'nests are created in 8_________where eggs are laid.', 'soil', 8),
(@group2_id, 8, 'the 9_________ of the käkāpō were used to make clothes.', 'feathers', 9),
(@group2_id, 8, '10_________were an animal which they introduced that ate the kākāpō\'s food sources.', 'deer', 10),
(@group2_id, 8, 'a definite sighting of female kākāpō on Rakiura Island was reported in the year 11_________', '1980', 11),
(@group2_id, 8, 'the Recovery Plan included an increase in 12_________', 'funding', 12),
(@group2_id, 8, 'a current goal of the Recovery Plan is to maintain the involvement of 13_________ in kākāpō protection.', 'stakeholders', 13);


-- -----------------------------------------------------------------
-- 🌳 PART 3: PASSAGE 2: To Britain (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES 
(
    @test_id, 
    'To Britain', 
    'Mark Rowe investigates attempts to reintroduce elms to Britain\nA. Around 25 million elms, accounting for 90% of all elm trees in the UK, died during the 1960s and \'70s of Dutch elm disease. ... [Sections A-G continued] ... Rather than plant new elms, the Woodland Trust emphasises providing space to those elms that have survived independently. \'Sometimes the best thing you can do is just give nature time to recover over time, you might get resistance,\' says Elliot.\n* horticultural analogue: a cultivated plant species that is genetically similar to an existing species', 
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage2_id, 'Questions 14-18: Reading Passage 2 has seven sections, A-G. Which section contains the following information? NB You may use any letter more than once.', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'reference to the research problems that arise from there being only a few surviving large elms', 'C', 14),
(@group3_id, 5, 'details of a difference of opinion about the value of reintroducing elms to Britain', 'G', 15),
(@group3_id, 5, 'reference to how Dutch elm disease was brought into Britain', 'B', 16),
(@group3_id, 5, 'a description of the conditions that have enabled a location in Britain to escape Dutch elm disease', 'E', 17),
(@group3_id, 5, 'reference to the stage at which young elms become vulnerable to Dutch elm disease', 'C', 18);

-- C. Group 4 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage2_id, 'Questions 19-23: Look at the following statements (19-23) and the list of people below. Match each statement with the correct person, A, B, or C.\nList of People: A. Matt Elliot, B. Karen Russell, C. Peter Bourne', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group4_id, 6, 'If a tree gets infected with Dutch elm disease, the damage rapidly becomes visible.', NULL, 'B', 19),
(@group4_id, 6, 'It may be better to wait and see if the mature elms that have survived continue to flourish.', NULL, 'A', 20),
(@group4_id, 6, 'There must be an explanation for the survival of some mature elms.', NULL, 'B', 21),
(@group4_id, 6, 'We need to be aware that insects carrying Dutch elm disease are not very far away.', NULL, 'C', 22),
(@group4_id, 6, 'You understand the effect Dutch elm disease has had when you see evidence of how prominent the tree once was.', NULL, 'A', 23);

-- D. Group 5 (Summary Completion - One Word Only)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage2_id, 'Questions 24-26: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group5_id, 8, 'For hundreds of years, the only tree that was more popular in Britain than elm was 24__________.', 'oak', 24),
(@group5_id, 8, 'Starting in the Bronze Age, many tools were made from elm and people also used it to make weapons. In the 18th century, it was grown to provide wood for boxes and 25__________.', 'flooring', 25),
(@group5_id, 8, 'Due to its strength, elm was often used for mining equipment and the Cutty Sark\'s 26__________ was also constructed from elm.', 'keel', 26);


-- -----------------------------------------------------------------
-- 🧠 PART 4: PASSAGE 3: How stress affects our judgement (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES 
(
    @test_id, 
    'How stress affects our judgement', 
    'Some of the most important decisions of our lives occur while we\'re feeling stressed and anxious. From medical decisions to financial and professional ones, we are all sometimes required to weigh up information under stressful conditions. ... [Full text of Passage 3 continued] ... The good news, however, is that positive emotions, such as hope, are contagious too, and are powerful in inducing people to act to find solutions. Being aware of the close relationship between people\'s emotional state and how they process information can help us frame our messages more effectively and become conscientious agents of change.', 
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage3_id, 'Questions 27-30: Choose the correct letter, A, B, C or D.', 1);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group6_id, 3, 'In the first paragraph, the writer introduces the topic of the text by', '{"A": "defining some commonly used terms.", "B": "questioning a widely held assumption.", "C": "mentioning a challenge faced by everyone.", "D": "specifying a situation which makes us most anxious."}', 'C', 27),
(@group6_id, 3, 'What point does the writer make about firefighters in the second paragraph?', '{"A": "The regular changes of stress levels in their working lives make them ideal study subjects.", "B": "The strategies they use to handle stress are of particular interest to researchers.", "C": "The stressful nature of their job is typical of many public service professions.", "D": "Their personalities make them especially well-suited to working under stress."}', 'A', 28),
(@group6_id, 3, 'What is the writer doing in the fourth paragraph?', '{"A": "explaining their findings", "B": "justifying their approach", "C": "setting out their objectives", "D": "describing their methodology"}', 'D', 29),
(@group6_id, 3, 'In the seventh paragraph, the writer describes a mechanism in the brain which', '{"A": "enables people to respond more quickly to stressful situations.", "B": "results in increased ability to control our levels of anxiety.", "C": "produces heightened sensitivity to indications of external threats.", "D": "is activated when there is a need to communicate a sense of danger."}', 'C', 30);

-- C. Group 7 (Matching Features - Sentence Endings)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage3_id, 'Questions 31-35: Complete each sentence with the correct ending, A-G, below.', 2);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features' (for sentence endings)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'At times when they were relaxed, the firefighters usually', 'B', 31),
(@group7_id, 6, 'The researchers noted that when the firefighters were stressed, they', 'G', 32),
(@group7_id, 6, 'When the firefighters were told good news, they always', 'F', 33),
(@group7_id, 6, 'The students\' cortisol levels and heart rates were affected when the researchers', 'E', 34),
(@group7_id, 6, 'In both experiments, negative information was processed better when the subjects', 'D', 35);

-- D. Group 8 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order) 
VALUES (@passage3_id, 'Questions 36-40: Do the following statements agree with the claims of the writer? YES/NO/NOT GIVEN', 3);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 2 for 'Yes/No/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 2, 'The tone of the content we post on social media tends to reflect the nature of the posts in our feeds.', 'YES', 36),
(@group8_id, 2, 'Phones have a greater impact on our stress levels than other electronic media devices.', 'NOT GIVEN', 37),
(@group8_id, 2, 'The more we read about a stressful public event on social media, the less able we are to take the information in.', 'NO', 38),
(@group8_id, 2, 'Stress created by social media posts can lead us to take unnecessary precautions.', 'YES', 39),
(@group8_id, 2, 'Our tendency to be affected by other people\'s moods can be used in a positive way.', 'YES', 40);

-- -----------------------------------------------------------------
-- 1. INITIAL SETUP: Insert the Test record and capture its ID
-- -----------------------------------------------------------------
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 2 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 🌊 PART 2: PASSAGE 1: Manatees (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Manatees',
    'Manatees, also known as sea cows, are aquatic mammals that belong to a group of animals called Sirenia. This group also contains dugongs. Dugongs and manatees look quite alike - they are similar in size, colour and shape, and both have flexible flippers for forelimbs. However, the manatee has a broad, rounded tail, whereas the dugong''s is fluked, like that of a whale. There are three species of manatees: the West Indian manatee (Trichechus manatus), the African manatee (Trichechus senegalensis) and the Amazonian manatee (Trichechus inunguis).\n\nUnlike most mammals, manatees have only six bones in their neck - most others, including humans and giraffes, have seven. This short neck allows a manatee to move its head up and down, but not side to side. To see something on its left or its right, a manatee must turn its entire body, steering with its flippers. Manatees have pectoral flippers but no back limbs, only a tail for propulsion. They do have pelvic bones, however–a leftover from their evolution from a four-legged to a fully aquatic animal. Manatees share some visual similarities to elephants. Like elephants, manatees have thick, wrinkled skin. They also have some hairs covering their bodies which help them sense vibrations in the water around them.\n\nSeagrasses and other marine plants make up most of a manatee''s diet. Manatees spend about eight hours each day grazing and uprooting plants. They eat up to 15% of their weight in food each day. African manatees are omnivorous - studies have shown that molluscs and fish make up a small part of their diets. West Indian and Amazonian manatees are both herbivores. \n\nManatees'' teeth are all molars - flat, rounded teeth for grinding food. Due to manatees'' abrasive aquatic plant diet, these teeth get worn down and they eventually fall out, so they continually grow new teeth that get pushed forward to replace the ones they lose. Instead of having incisors to grasp their food, manatees have lips which function like a pair of hands to help tear food away from the seafloor. \n\nManatees are fully aquatic, but as mammals, they need to come up to the surface to breathe. When awake, they typically surface every two to four minutes, but they can hold their breath for much longer. Adult manatees sleep underwater for 10-12 hours a day, but they come up for air every 15-20 minutes. Active manatees need to breathe more frequently. It''s thought that manatees use their muscular diaphragm and breathing to adjust their buoyancy. They may use diaphragm contractions to compress and store gas in folds in their large intestine to help them float.\n\nThe West Indian manatee reaches about 3.5 metres long and weighs on average around 500 kilogrammes. It moves between fresh water and salt water, taking advantage of coastal mangroves and coral reefs, rivers, lakes and inland lagoons. There are two subspecies of West Indian manatee: the Antillean manatee is found in waters from the Bahamas to Brazil, whereas the Florida manatee is found in US waters, although some individuals have been recorded in the Bahamas. In winter, the Florida manatee is typically restricted to Florida. When the ambient water temperature drops below 20°C, it takes refuge in naturally and artificially warmed water, such as at the warm-water outfalls from powerplants.\n\nThe African manatee is also about 3.5 metres long and found in the sea along the west coast of Africa, from Mauritania down to Angola. The species also makes use of rivers, with the mammals seen in landlocked countries such as Mali and Niger. The Amazonian manatee is the smallest species, though it is still a big animal. It grows to about 2.5 metres long and 350 kilogrammes. Amazonian manatees favour calm, shallow waters that are above 23°C. This species is found in fresh water in the Amazon Basin in Brazil, as well as in Colombia, Ecuador and Peru.\n\nAll three manatee species are endangered or at a heightened risk of extinction. The African manatee and Amazonian manatee are both listed as Vulnerable by the International Union for Conservation of Nature (IUCN). It is estimated that 140,000. Amazonian manatees were killed between 1935 and 1954 for their meat, fat and skin with the latter used to make leather. In more recent years, African manatee decline has been tied to incidental capture in fishing nets and hunting. Manatee hunting is now illegal in every country the African species is found in.\n\nThe two subspecies of West Indian manatee are listed as Endangered by the IUCN. Both are also expected to undergo a decline of 20% over the next 40 years. A review of almost 1,800 cases of entanglement in fishing nets and of plastic consumption among marine mammals in US waters from 2009 to 2020 found that at least 700 cases involved manatees. The chief cause of death in Florida manatees is boat strikes. However, laws in certain parts of Florida now limit boat speeds during winter, allowing slow-moving manatees more time to respond.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Summary Completion - ONE WORD AND/OR A NUMBER)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-6: Complete the notes below. Choose ONE WORD AND/OR A NUMBER from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'look similar to dugongs, but with a differently shaped 1__________', 'tail', 1),
(@group1_id, 8, 'need to use their 2__________ to help to turn their bodies around in order to look sideways', 'flippers', 2),
(@group1_id, 8, 'sense vibrations in the water by means of 3__________ on their skin', 'hairs', 3),
(@group1_id, 8, 'eat mainly aquatic vegetation, such as 4__________', 'seagrasses', 4),
(@group1_id, 8, 'grasp and pull up plants with their 5__________', 'lips', 5),
(@group1_id, 8, 'may regulate the 6__________ of their bodies by using muscles of diaphragm to store air internally', 'buoyancy', 6);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 7-13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'West Indian manatees can be found in a variety of different aquatic habitats.', 'TRUE', 7),
(@group2_id, 1, 'The Florida manatee lives in warmer waters than the Antillean manatee.', 'NOT GIVEN', 8),
(@group2_id, 1, 'The African manatee''s range is limited to coastal waters between the West African countries of Mauritania and Angola.', 'FALSE', 9),
(@group2_id, 1, 'The extent of the loss of Amazonian manatees in the mid-twentieth century was only revealed many years later.', 'NOT GIVEN', 10),
(@group2_id, 1, 'It is predicted that West Indian manatee populations will fall in the coming decades.', 'TRUE', 11),
(@group2_id, 1, 'The risk to manatees from entanglement and plastic consumption increased significantly in the period 2009-2020.', 'NOT GIVEN', 12),
(@group2_id, 1, 'There is some legislation in place which aims to reduce the likelihood of boat strikes on manatees in Florida.', 'TRUE', 13);


-- -----------------------------------------------------------------
-- 🕰️ PART 3: PASSAGE 2: Procrastination (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2 (shortened for brevity)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Procrastination',
    'A psychologist explains why we put off important tasks and how we can break this habit\n\nA. Procrastination is the habit of delaying a necessary task, usually by focusing on less urgent, more enjoyable, and easier activities instead. We all do it from time to time... [Full text continued]... This can all make it easier to get back on track.',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-16: Reading Passage 2 has six paragraphs, A-F. Which paragraph contains the following information? NB You may use any letter more than once.', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'mention of false assumptions about why people procrastinate', 'B', 14),
(@group3_id, 5, 'reference to the realisation that others also procrastinate', 'F', 15),
(@group3_id, 5, 'neurological evidence of a link between procrastination and emotion', 'B', 16);

-- C. Group 4 (Summary Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 17-22: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 8, 'Many people think that procrastination is the result of 17__________.', 'laziness', 17),
(@group4_id, 8, '...cause us to feel 18__________ when we think about them.', 'anxious', 18),
(@group4_id, 8, '...identifying 19__________.', 'threats', 19),
(@group4_id, 8, 'Getting ready to take 20__________ might be a typical example of one such task.', 'exams', 20),
(@group4_id, 8, 'People who are likely to procrastinate tend to be either 21__________ those with low self-esteem.', 'perfectionists', 21),
(@group4_id, 8, 'It''s often followed by a feeling of 22__________, which worsens our mood and leads to more procrastination.', 'guilt', 22);

-- D. Group 5 (Multiple Choice - Choose TWO)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23-26: Choose TWO letters, A-E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO comparisons between employees who often procrastinate and those who do not are mentioned in the text?', '{"A": "Their salaries are lower.", "B": "The quality of their work is inferior.", "C": "They don''t keep their jobs for as long.", "D": "They don''t enjoy their working lives as much.", "E": "They have poorer relationships with colleagues."}', 'A, C', 23),
(@group5_id, 3, 'Which TWO recommendations for getting out of a cycle of procrastination does the writer give?', '{"A": "not judging ourselves harshly", "B": "setting ourselves manageable aims", "C": "rewarding ourselves for tasks achieved", "D": "prioritising tasks according to their importance", "E": "avoiding things that stop us concentrating on our tasks"}', 'A, E', 24);


-- -----------------------------------------------------------------
-- 🤖 PART 4: PASSAGE 3: Invasion of the Robot Umpires (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3 (shortened for brevity)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Invasion of the Robot Umpires',
    'A few years ago, Fred DeJesus from Brooklyn, New York became the first umpire in a minor league baseball game to use something called the Automated Ball-Strike System (ABS)... [Full text continued]... What is there to talk about?"',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-32: Do the following statements agree with the claims of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 1);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 2 for 'Yes/No/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 2, 'When DeJesus first used ABS, he shared decision-making about strikes with it.', 'NO', 27),
(@group6_id, 2, 'MLB considered it necessary to amend the size of the strike zone when criticisms were received from players.', 'YES', 28),
(@group6_id, 2, 'MLB is keen to justify the money spent on improving the accuracy of ABS''s calculations.', 'NOT GIVEN', 29),
(@group6_id, 2, 'The hundred-mile-an-hour fastball led to a more exciting style of play.', 'NO', 30),
(@group6_id, 2, 'The differing proposals for alterations to the baseball bat led to fierce debate on Sword''s team.', 'NOT GIVEN', 31),
(@group6_id, 2, 'ABS makes changes to the shape of the strike zone feasible.', 'YES', 32);

-- C. Group 7 (Summary Completion - Matching Phrases)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 33-37: Complete the summary using the list of phrases, A-H, below. A. pitch boundary, B. numerous disputes, C. team tactics, D. subjective assessment, E. widespread approval, F. former roles, G. total silence, H. perceived area', 2);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion' or 6 for 'Matching Features' (using the letter of the correct option as answer)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 8, 'Even after ABS was developed, MLB still wanted human umpires to shout out decisions as they had in their 33__________.', 'F', 33),
(@group7_id, 8, 'The umpire''s job had, at one time, required a 34__________ about whether a ball was a strike.', 'D', 34),
(@group7_id, 8, 'A ball is considered a strike when the batter does not hit it and it crosses through a 35__________ extending approximately from the batter''s knee to his chest.', 'H', 35),
(@group7_id, 8, 'In the past, 36__________ over strike calls were not uncommon, but today everyone accepts the complete ban on pushing or shoving the umpire.', 'B', 36),
(@group7_id, 8, 'One difference, however, is that during the first game DeJesus used ABS, strike calls were met with 37__________.', 'G', 37);

-- D. Group 8 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 38-40: Choose the correct letter, A, B, C or D.', 3);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group8_id, 3, 'What does the writer suggest about ABS in the fifth paragraph?', '{"A": "It is bound to make key decisions that are wrong.", "B": "It may reduce some of the appeal of the game.", "C": "It will lead to the disappearance of human umpires.", "D": "It may increase calls for the rules of baseball to be changed."}', 'B', 38),
(@group8_id, 3, 'Morgan Sword says that the introduction of ABS', '{"A": "was regarded as an experiment without a guaranteed outcome.", "B": "was intended to keep up with developments in other sports.", "C": "was a response to changing attitudes about the role of sport.", "D": "was an attempt to ensure baseball retained a young audience."}', 'D', 39),
(@group8_id, 3, 'Why does the writer include the views of Noë and Russo?', '{"A": "to show that attitudes to technology vary widely", "B": "to argue that people have unrealistic expectations of sport", "C": "to indicate that accuracy is not the same thing as enjoyment", "D": "to suggest that the number of baseball fans needs to increase"}', 'C', 40);

-- -----------------------------------------------------------------
-- START OF FILE: IELTS Reading Test 3 SQL Insert Script
-- -----------------------------------------------------------------

-- 1. INITIAL SETUP: Insert the Test record and capture its ID
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 3 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- -----------------------------------------------------------------
-- 🧊 PART 2: PASSAGE 1: Frozen Food (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Frozen Food',
    'At some point in history, humans discovered that ice preserved food. There is evidence that winter ice was stored to preserve food in the summer as far back as 10,000 years ago. Two thousand years ago, the inhabitants of South America''s Andean mountains had a unique means of conserving potatoes for later consumption. They froze them overnight, then trampled them to squeeze out the moisture, then dried them in the sun. This preserved their nutritional value-if not their aesthetic appeal.\n\nNatural ice remained the main form of refrigeration until late in the 19th century. In the early 1800s, ship owners from Boston, USA, had enormous blocks of Arctic ice towed all over the Atlantic for the purpose of food preservation. In 1851, railroads first began putting blocks of ice in insulated rail cars to send butter from Ogdensburg, New York, to Boston.\n\nFinally, in 1870, Australian inventors found a way to make ''mechanical ice''. They used a compressor to force a gas-ammonia at first and later Freon-through a condenser. The compressed gas gave up some of its heat as it moved through the condenser. Then the gas was released quickly into a low-pressure evaporator coil where it became liquid and cold. Air was blown over the evaporator coil and then this cooled air passed into an insulated compartment, lowering its temperature to freezing point.\n\nInitially, this process was invented to keep Australian beer cool even in hot weather. But Australian cattlemen were quick to realize that, if they could put this new invention on a ship, they could export meat across the oceans. In 1880, a shipment of Australian beef and mutton was sent, frozen, to England. While the food frozen this way was still palatable, there was some deterioration. During the freezing process, crystals formed within the cells of the food, and when the ice expanded and the cells burst, this spoilt the flavor and texture of the food.\n\nThe modern frozen food industry began with the indigenous Inuit people of Canada. In 1912, a biology student in Massachusetts, USA, named Clarence Birdseye, ran out of money and went to Labrador in Canada to trap and trade furs. While he was there, he became fascinated with how the Inuit would quickly freeze fish in the Arctic air. The fish looked and tasted fresh even months later.\n\nBirdseye returned to the USA in 1917 and began developing mechanical freezers capable of quick-freezing food. Birdseye methodically kept inventing better freezers and gradually built a business selling frozen fish from Gloucester, Massachusetts. In 1929, his business was sold and became General Foods, but he stayed with the company as director of research, and his division continued to innovate.\n\nBirdseye was responsible for several key innovations that made the frozen food industry possible. He developed quick-freezing techniques that reduced the damage that crystals caused, as well as the technique of freezing the product in the package it was to be sold in. He also introduced the use of cellophane, the first transparent material for food packaging, which allowed consumers to see the quality of the product. Birdseye products also came in convenient size packages that could be prepared with a minimum of effort.\n\nBut there were still obstacles. In the 1930s, few grocery stores could afford to buy freezers for a market that wasn''t established yet. So, Birdseye leased inexpensive freezer cases to them. He also leased insulated railroad cars so that he could ship his products nationwide. However, few consumers had freezers large enough or efficient enough to take advantage of the products.\n\nSales increased in the early 1940s, when World War II gave a boost to the frozen food industry because tin was being used for munitions. Canned foods were rationed to save tin for the war effort, while frozen foods were abundant and cheap. Finally, by the 1950s, refrigerator technology had developed far enough to make these appliances affordable for the average family. By 1953, 33 million US families owned a refrigerator, and manufacturers were gradually increasing the size of the freezer compartments in them.\n\n1950s families were also looking for convenience at mealtimes, so the moment was right for the arrival of the ''TV Dinner''. Swanson Foods was a large, nationally recognized producer of canned and frozen poultry. In 1954, the company adapted some of Birdseye''s freezing techniques, and with the help of a clever name and a huge advertising budget, it launched the first ''TV Dinner''. This consisted of frozen turkey, potatoes and vegetables served in the same segmented aluminum tray that was used by airlines. The product was an instant success. Within a year, Swanson had sold 13 million TV dinners. American consumers couldn''t resist the combination of a trusted brand name, a single-serving package and the convenience of a meal that could be ready after only 25 minutes in a hot oven. By 1959, Americans were spending $2.7 billion annually on frozen foods, and half a billion of that was spent on ready-prepared meals such as the TV Dinner.\n\nToday, the US frozen food industry has a turnover of over $67 billion annually, with $26.6 billion of that sold to consumers for home consumption. The remaining $40 billion in frozen food sales come through restaurants, cafeterias, hospitals and schools, and that represents a third of the total food service sales.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Summary Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'People conserved the nutritional value of 1__________, using a method of freezing then drying.', 'potatoes', 1),
(@group1_id, 8, '2__________ was kept cool by ice during transportation in specially adapted trains.', 'butter', 2),
(@group1_id, 8, 'Two kinds of 3__________ were the first frozen food shipped to England.', 'meat', 3),
(@group1_id, 8, 'quick-freezing methods, so that 4__________ did not spoil the food.', 'crystals', 4),
(@group1_id, 8, 'packaging products with 5__________ so the product was visible.', 'cellophane', 5),
(@group1_id, 8, 'Frozen food became popular because of a shortage of 6__________', 'tin', 6),
(@group1_id, 8, 'A large number of homes now had a 7__________', 'refrigerator', 7);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'The ice transportation business made some Boston ship owners very wealthy in the early 1800s.', 'NOT GIVEN', 8),
(@group2_id, 1, 'A disadvantage of the freezing process invented in Australia was that it affected the taste of food.', 'TRUE', 9),
(@group2_id, 1, 'Clarence Birdseye travelled to Labrador in order to learn how the Inuit people froze fish.', 'FALSE', 10),
(@group2_id, 1, 'Swanson Foods invested a great deal of money in the promotion of the TV Dinner.', 'TRUE', 11),
(@group2_id, 1, 'Swanson Foods developed a new style of container for the launch of the TV Dinner.', 'FALSE', 12),
(@group2_id, 1, 'The US frozen food industry is currently the largest in the world.', 'NOT GIVEN', 13);

-- -----------------------------------------------------------------
-- 🐠 PART 3: PASSAGE 2: Can the planet's coral reefs be saved? (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Can the planet''s coral reefs be saved?',
    'Conservationists have put the final touches to a giant artificial reef they have been assembling at the world-renowned Zoological Society of London (London Zoo)... [Full text inserted here]... The crucial point is that the progress we make in making coral better able to survive in a warming world can be shown to the public and encourage them to believe that we can do something to save the planet''s reefs,'' said Pearce-Kelly. ''Saving our coral reefs is now a critically important ecological goal.''',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Headings)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-19: Reading Passage 2 has six sections, A-F. Choose the correct heading for each section from the list of headings below.', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 7 for 'Matching Headings'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 7, 'Section A', 'v', 14), -- Two clear educational goals
(@group3_id, 7, 'Section B', 'ii', 15), -- Cooperation beneath the waves
(@group3_id, 7, 'Section C', 'iv', 16), -- Disagreement about the accuracy of a certain phrase
(@group3_id, 7, 'Section D', 'vii', 17), -- A warning of further trouble ahead
(@group3_id, 7, 'Section E', 'iii', 18), -- Working to lessen the problems
(@group3_id, 7, 'Section F', 'vi', 19); -- Promoting hope

-- C. Group 4 (Multiple Choice - Choose TWO - Causes of Damage)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 20 and 21: Choose TWO letters, A-E.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group4_id, 3, 'Which TWO of these causes of damage to coral reefs are mentioned by the writer of the text?', '{"A": "a rising number of extreme storms", "B": "the removal of too many fish from the sea", "C": "the contamination of the sea from waste", "D": "increased disease among marine species", "E": "alterations in the usual flow of water in the seas"}', 'C, E', 20);

-- D. Group 5 (Multiple Choice - Choose TWO - London Zoo Researchers)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 22 and 23: Choose TWO letters, A-E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO of the following statements are true of the researchers at London Zoo?', '{"A": "They are hoping to expand the numbers of different corals being bred in laboratories.", "B": "They want to identify corals that can cope well with the changed sea conditions.", "C": "They are looking at ways of creating artificial reefs that corals could grow on.", "D": "They are trying out methods that would speed up reproduction in some corals.", "E": "They are investigating materials that might protect reefs from higher temperatures."}', 'B, D', 22);

-- E. Group 6 (Sentence Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 24-26: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 4);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 8, 'Corals have a number of 24__________ which they use to collect their food.', 'tentacles', 24),
(@group6_id, 8, 'Algae gain 25__________ from being inside the coral.', 'protection', 25),
(@group6_id, 8, 'Increases in the warmth of the sea water can remove the 26__________ from coral.', 'colour', 26);

-- -----------------------------------------------------------------
-- 🤖 PART 4: PASSAGE 3: Robots and us (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Robots and us',
    'Three leaders in their fields answer questions about our relationships with robot\n\nWhen asked ''Should robots be used to colonise other planets?'', cosmology and astrophysics Professor Martin Rees said he believed the solar system would be mapped by robotic craft by the end of the century... [Full text inserted here]... In some cases, we need to bring these different understandings together to get a whole perspective. Perhaps then, we won''t be so frightened that something we create as a copy of ourselves will be a [threat] to us.',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 7 (Matching Features - Experts)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-33: Look at the following statements and the list of experts below. Match each statement with the correct expert, A, B or C. List of Experts: A. Martin Rees, B. Daniel Wolpert, C. Kathleen Richardson', 1);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'For our own safety, humans will need to restrict the abilities of robots.', 'A', 27),
(@group7_id, 6, 'The risk of robots harming us is less serious than humans believe it to be.', 'C', 28),
(@group7_id, 6, 'It will take many decades for robot intelligence to be as imaginative as human intelligence.', 'B', 29),
(@group7_id, 6, 'We may have to start considering whether we are treating robots fairly.', 'A', 30),
(@group7_id, 6, 'Robots are probably of more help to us on Earth than in space.', 'B', 31),
(@group7_id, 6, 'The ideas in high-quality science fiction may prove to be just as accurate as those found in the work of mediocre scientists.', 'A', 32),
(@group7_id, 6, 'There are those who look forward to robots developing greater intelligence.', 'C', 33);

-- C. Group 8 (Matching Features - Sentence Endings)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 34-36: Complete each sentence with the correct ending, A-D, below. A. robots to explore outer space. B. advances made in machine intelligence so far. C. changes made to other planets for our own benefit. D. the harm already done by artificial intelligence.', 2);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 6, 'Richardson and Rees express similar views regarding the ethical aspect of', 'C', 34),
(@group8_id, 6, 'Rees and Wolpert share an opinion about the extent of', 'B', 35),
(@group8_id, 6, 'Wolpert disagrees with Richardson on the question of', 'D', 36);

-- D. Group 9 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37-40: Choose the correct letter, A, B, C or D.', 3);
SET @group9_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group9_id, 3, 'What point does Richardson make about fear of machines?', '{"A": "It has grown alongside the development of ever more advanced robots.", "B": "It is the result of our inclination to attribute human characteristics to non-human entities.", "C": "It has its origins in basic misunderstandings about how inanimate objects function.", "D": "It demonstrates a key difference between human intelligence and machine intelligence."}', 'B', 37),
(@group9_id, 3, 'What potential advance does Rees see as a cause for concern?', '{"A": "robots outnumbering people", "B": "robots having abilities which humans do not", "C": "artificial intelligence developing independent thought", "D": "artificial intelligence taking over every aspect of our lives"}', 'C', 38),
(@group9_id, 3, 'What does Wolpert emphasise in his response to the question about science fiction?', '{"A": "how science fiction influences our attitudes to robots", "B": "how fundamental robots are to the science fiction genre", "C": "how the image of robots in science fiction has changed over time", "D": "how reactions to similar portrayals of robots in science fiction may vary"}', 'B', 39),
(@group9_id, 3, 'What is Richardson doing in her comment about reality and fantasy?', '{"A": "warning people not to confuse one with the other", "B": "outlining ways in which one has impacted on the other", "C": "recommending a change of approach in how people view them", "D": "explaining why scientists have a different perspective on them from other people"}', 'C', 40);

-- -----------------------------------------------------------------
-- 1. INITIAL SETUP: Insert the Test record and capture its ID
-- -----------------------------------------------------------------
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 4 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 🌸 PART 2: PASSAGE 1: Georgia O'Keeffe (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Georgia O''Keeffe',
    'For seven decades, Georgia O''Keeffe (1887-1986) was a major figure in American art. Remarkably, she remained independent from shifting art trends and her work stayed true to her own vision, which was based on finding the essential, abstract forms in nature... [Full text inserted here]... O''Keeffe died in 1986 at the age of ninety-eight, but her rich legacy of some 900 paintings has continued to attract subsequent generations of artists and art lovers who derive inspiration from these very American images.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Notes Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary/Notes Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'studied art, then worked as a 1__________ in various places in the USA', 'teacher', 1),
(@group1_id, 8, 'created drawings using 2__________ which were exhibited in New York City', 'charcoal', 2),
(@group1_id, 8, 'moved to New York and became famous for her paintings of the city''s 3__________', 'skyscrapers', 3),
(@group1_id, 8, 'produced a series of innovative close-up paintings of 4__________', 'flowers', 4),
(@group1_id, 8, 'went to New Mexico and was initially inspired to paint the many 5__________ that could be found there', 'bones', 5),
(@group1_id, 8, 'continued to paint various features that together formed the dramatic 6__________ of New Mexico for over forty years', 'landscape', 6),
(@group1_id, 8, 'travelled widely by plane in later years, and painted pictures of clouds and 7__________ seen from above', 'rivers', 7);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'Georgia O''Keeffe''s style was greatly influenced by the changing fashions in art over the seven decades of her career.', 'FALSE', 8),
(@group2_id, 1, 'When O''Keeffe finished high school, she had already made her mind up about the career that she wanted.', 'TRUE', 9),
(@group2_id, 1, 'Alfred Stieglitz first discovered O''Keeffe''s work when she sent some abstract drawings to his gallery in New York City.', 'FALSE', 10),
(@group2_id, 1, 'O''Keeffe was the subject of Stieglitz''s photographic work for many years.', 'TRUE', 11),
(@group2_id, 1, 'O''Keeffe''s paintings of the patio of her house in Abiquiú were among the artist''s favourite works.', 'NOT GIVEN', 12),
(@group2_id, 1, 'O''Keeffe produced a greater quantity of work during the 1950s to 1970s than at any other time in her life.', 'NOT GIVEN', 13);


-- -----------------------------------------------------------------
--  Adapt. PART 3: Adapting to the effects of climate change (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Adapting to the effects of climate change',
    'All around the world, nations are already preparing for, and adapting to, climate change and its impacts... [Full text inserted here]... Spotts says one of these streets, in the Winnetka neighbourhood of San Fernando Valley, can now be seen as a pale crescent, the only cool spot on an otherwise red thermal image, from the International Space Station.',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-17: Reading Passage 2 has six paragraphs, A-F. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'how a type of plant functions as a natural protection for coastlines', 'C', 14),
(@group3_id, 5, 'a prediction about how long it could take to stop noticing the effects of climate change', 'A', 15),
(@group3_id, 5, 'a reference to the fact that a solution is particularly cost-effective', 'D', 16),
(@group3_id, 5, 'a mention of a technology used to locate areas most in need of intervention', 'F', 17);

-- C. Group 4 (Sentence Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 18-22: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Sentence Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 8, 'The stormwater-management programme in Miami Beach has involved the installation of efficient 18.__________.', 'pumps', 18),
(@group4_id, 8, 'The construction of 19__________ was the first stage of a project to ensure the success of mangroves in Indonesia.', 'dams', 19),
(@group4_id, 8, 'As a response to rising floodwaters in the Mekong Delta, a not-for-profit organisation has been building houses that can 20__________', 'float', 20),
(@group4_id, 8, 'Rising sea levels in Bangladesh have made it necessary to introduce various 21__________ that are suitable for areas of high salt content.', 'crops', 21),
(@group4_id, 8, 'A project in LA has increased the number of 22__________ on the city''s streets.', 'trees', 22);

-- D. Group 5 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23-26: Look at the following statements (23-26) and the list of people below. Match each statement with the correct person, A-E. List of People: A. Yanira Pineda, B. Susanna Tol, C. Elizabeth English, D. Raisa Chowdhury, E. Greg Spotts', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group5_id, 6, 'It is essential to adopt strategies which involve and help residents of the region.', 'B', 23),
(@group5_id, 6, 'Interventions which reduce heat are absolutely vital for our survival in this location.', 'E', 24),
(@group5_id, 6, 'More work will need to be done in future decades to deal with the impact of rising water levels.', 'A', 25),
(@group5_id, 6, 'The number of locations requiring action to adapt to flooding has grown in recent years.', 'C', 26);


-- -----------------------------------------------------------------
-- 🐕 PART 4: PASSAGE 3: A new role for livestock guard dogs (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'A new role for livestock guard dogs',
    'Livestock guard dogs, traditionally used to protect farm animals from predators, are now being used to protect the predators themselves... [Full text inserted here]... Indeed, Australian researchers are now using dogs to enhance biodiversity and create refuges for species threatened by predation. So if we can get this right, there may be a bright future for guard dogs in promoting harmonious coexistence between humans and wildlife.',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-31: Reading Passage 3 has seven paragraphs, A-G. Which paragraph contains the following information? NB You may use any letter more than once.', 1);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 5, 'an example of how one predator has been protected by the introduction of livestock guard dogs', 'D', 27),
(@group6_id, 5, 'an optimistic suggestion about the possible positive developments in the use of livestock guard dogs', 'G', 28),
(@group6_id, 5, 'a description of how the methods used by livestock guard dogs help to keep predators away', 'B', 29),
(@group6_id, 5, 'claims by different academics that the use of livestock guard dogs is a successful way of protecting farmers'' herds', 'C', 30),
(@group6_id, 5, 'a reference to how livestock guard dogs gain their skills', 'B', 31);

-- C. Group 7 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 32-36: Look at the following statements (32-36) and the list of people below. Match each statement with the correct person, A-E. List of people: A. Dan Macon, B. Silvia Ribeiro, C. Linda van Bommel, D. Julie Young, E. Bethany Smith', 2);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'The use of guard dogs may save the lives of both livestock and wild animals.', 'D', 32),
(@group7_id, 6, 'Claims of a change in behaviour from those using livestock guard dogs may not be totally accurate.', 'E', 33),
(@group7_id, 6, 'There may be negative results if the use of livestock guard dogs is not sufficiently widespread.', 'B', 34),
(@group7_id, 6, 'Livestock guard dogs are the best way of protecting farm animals, as long as the dogs are appropriately handled.', 'C', 35),
(@group7_id, 6, 'Teaching a livestock guard dog how to do its work needs a different focus from teaching a house guard dog.', 'A', 36);

-- D. Group 8 (Summary Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37-40: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 3);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 8, 'In Namibia, livestock guard dogs have been used to protect domestic animals from attacks by cheetahs. This has led to a rise in the deaths of other predators, particularly 37__________.', 'jackals', 37),
(@group8_id, 8, 'In addition, it has been suggested that the dogs could have 38__________ which may affect other species, and that they may reduce the amount of 39__________ available to certain wild animals.', 'diseases', 38),
(@group8_id, 8, 'In addition, it has been suggested that the dogs could have 38__________ which may affect other species, and that they may reduce the amount of 39__________ available to certain wild animals.', 'food', 39),
(@group8_id, 8, 'On the other hand, these dogs may help birds by protecting their nests. These might otherwise be threatened by predators such as 40__________.', 'foxes', 40);

-- -----------------------------------------------------------------
-- 1. INITIAL SETUP: Insert the Test record and capture its ID
-- -----------------------------------------------------------------
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 5 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 🎾 PART 2: PASSAGE 1: How tennis rackets have changed (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'How tennis rackets have changed',
    'In 2016, the British professional tennis player Andy Murray was ranked as the world’s number one. It was an incredible achievement by any standard – made even more remarkable by the fact that he did this during a period considered to be one of the strongest in the sport’s history... [Full text inserted here]... Racket customisation and modification have pushed the standards of the game to greater levels that few could have anticipated in the days of natural strings and heavy, wooden frames, and it’s exciting to see what further developments there will be in the future.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1–7: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 1, 'People had expected Andy Murray to become the world’s top tennis player for at least five years before 2016.', 'FALSE', 1),
(@group1_id, 1, 'The change that Andy Murray made to his rackets attracted a lot of attention.', 'FALSE', 2),
(@group1_id, 1, 'Most of the world’s top players take a professional racket stringer on tour with them.', 'NOT GIVEN', 3),
(@group1_id, 1, 'Mike and Bob Bryan use rackets that are light in comparison to the majority of rackets.', 'FALSE', 4),
(@group1_id, 1, 'Werner Fischer played with a spaghetti-strung racket that he designed himself.', 'NOT GIVEN', 5),
(@group1_id, 1, 'The weather can affect how professional players adjust the strings on their rackets.', 'TRUE', 6),
(@group1_id, 1, 'It was believed that the change Pete Sampras made to his rackets contributed to his strong serve.', 'TRUE', 7);

-- C. Group 2 (Notes Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8–13: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary/Notes Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 8, 'Mike and Bob Bryan made changes to the types of 8__________ used on their racket frames.', 'paint', 8),
(@group2_id, 8, 'Players were not allowed to use the spaghetti-strung racket because of the amount of 9__________ it created.', 'topspin', 9),
(@group2_id, 8, 'Changes to rackets can be regarded as being as important as players’ diets or the 10__________ they do.', 'training', 10),
(@group2_id, 8, 'All rackets used to have natural strings made from the 11__________ of animals.', 'gut', 11),
(@group2_id, 8, 'Pete Sampras had metal 12__________ put into the frames of his rackets.', 'weights', 12),
(@group2_id, 8, 'Gonçalo Oliveira changed the 13__________ on his racket handles.', 'grips', 13);


-- -----------------------------------------------------------------
-- 🏴‍☠️ PART 3: PASSAGE 2: The pirates of the ancient Mediterranean (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The pirates of the ancient Mediterranean',
    'When one mentions pirates, an image springs to most people’s minds of a crew of misfits, daredevils and adventurers in command of a tall sailing ship in the Caribbean Sea... [Full text inserted here]... Instead of a maritime menace, Rome got productive farmers that further boosted its economy.',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14–19: Reading Passage 2 has seven paragraphs, A–G. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'a reference to a denial of involvement in piracy', 'D', 14),
(@group3_id, 5, 'details of how a campaign to eradicate piracy was carried out', 'G', 15),
(@group3_id, 5, 'a mention of the circumstances in which states in the ancient world would make use of pirates', 'C', 16),
(@group3_id, 5, 'a reference to how people today commonly view pirates', 'A', 17),
(@group3_id, 5, 'an explanation of how some people were encouraged not to return to piracy', 'G', 18),
(@group3_id, 5, 'a mention of the need for many sailing vessels to stay relatively close to land', 'B', 19);

-- C. Group 4 (Multiple Choice - Choose TWO)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 20 and 21: Choose TWO letters, A–E.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group4_id, 3, 'Which TWO of the following statements does the writer make about inhabitants of the Mediterranean region in the ancient world?', '{"A": "They often used stolen vessels to carry out pirate attacks.", "B": "They managed to escape capture by the authorities because they knew the area so well.", "C": "They paid for information about the routes merchant ships would take.", "D": "They depended more on the sea for their livelihood than on farming.", "E": "They stored many of the goods taken in pirate attacks in coves along the coastline."}', 'B, D', 20);

-- D. Group 5 (Multiple Choice - Choose TWO)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 22 and 23: Choose TWO letters, A–E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO of the following statements does the writer make about piracy and ancient Greece?', '{"A": "The state estimated that very few people were involved in piracy.", "B": "Attitudes towards piracy changed shortly after the Iliad and the Odyssey were written.", "C": "Important officials were known to occasionally take part in piracy.", "D": "Every citizen regarded pirate attacks on cities as unacceptable.", "E": "A favourable view of piracy is evident in certain ancient Greek texts."}', 'C, E', 22);

-- E. Group 6 (Summary Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 24–26: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 4);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 8, 'However, attacks on vessels transporting 24__________ to Rome resulted in calls for 25__________for the pirates responsible. Nevertheless, piracy continued, with some pirates demanding a 26__________ for the return of the Roman officials they captured.', 'grain', 24),
(@group6_id, 8, 'However, attacks on vessels transporting 24__________ to Rome resulted in calls for 25__________for the pirates responsible. Nevertheless, piracy continued, with some pirates demanding a 26__________ for the return of the Roman officials they captured.', 'punishment', 25),
(@group6_id, 8, 'However, attacks on vessels transporting 24__________ to Rome resulted in calls for 25__________for the pirates responsible. Nevertheless, piracy continued, with some pirates demanding a 26__________ for the return of the Roman officials they captured.', 'ransom', 26);


-- -----------------------------------------------------------------
-- 🧠 PART 4: PASSAGE 3: The persistence and peril of misinformation (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The persistence and peril of misinformation',
    'Misinformation – both deliberately promoted and accidentally shared – is perhaps an inevitable part of the world in which we live, but it is not a new problem... [Full text inserted here]... To overcome the worst effects of the phenomenon, we will need coordinated efforts over time, rather than any singular one-time panacea we could hope to offer.',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 7 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27–30: Choose the correct letter, A, B, C or D.', 1);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group7_id, 3, 'What point does the writer make about misinformation in the first paragraph?', '{"A": "Misinformation is a relatively recent phenomenon.", "B": "Some people find it easy to identify misinformation.", "C": "Misinformation changes as it is passed from one person to another.", "D": "There may be a number of reasons for the spread of misinformation."}', 'D', 27),
(@group7_id, 3, 'What does the writer say about the role of technology?', '{"A": "It may at some point provide us with a solution to misinformation.", "B": "It could fundamentally alter the way in which people regard information.", "C": "It has changed the way in which organisations use misinformation.", "D": "It has made it easier for people to check whether information is accurate."}', 'A', 28),
(@group7_id, 3, 'What is the writer doing in the fourth paragraph?', '{"A": "comparing the different opinions people have of misinformation.", "B": "explaining how the effects of misinformation have changed over time", "C": "outlining which issues connected with misinformation are significant today", "D": "describing the attitude of policy makers towards misinformation in the media"}', 'C', 29),
(@group7_id, 3, 'What point does the writer make about regulation in the USA?', '{"A": "The guidelines issued by the FDA need to be simplified.", "B": "Regulation does not affect people’s opinions of new prescription drugs.", "C": "The USA has more regulatory bodies than most other countries.", "D": "Regulation fails to prevent misinformation from appearing in the media."}', 'D', 30);

-- C. Group 8 (Summary Completion - Matching Phrases)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 31–36: Complete the summary using the list of phrases, A–J, below. A. constant conflict, B. additional evidence, C. different locations, D. experimental subjects, E. short period, F. extreme distrust, G. frequent exposure, H. mental operation, I. dubious reason, J. different ideas', 2);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion' (matching phrases)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 8, 'Although people have 31__________ to misinformation, there is debate about precisely how and when we label something as true or untrue.', 'G', 31),
(@group8_id, 8, 'The philosophers Descartes and Spinoza had 32__________ about how people engage with information.', 'J', 32),
(@group8_id, 8, 'Moreover, Spinoza believes that a distinct 33__________ is involved in these stages.', 'H', 33),
(@group8_id, 8, 'Recent research has provided 34__________ for Spinoza’s theory and it would appear that people accept all encountered information as if it were true, even if this is for an extremely 35__________', 'B', 34),
(@group8_id, 8, 'Recent research has provided 34__________ for Spinoza’s theory and it would appear that people accept all encountered information as if it were true, even if this is for an extremely 35__________', 'E', 35),
(@group8_id, 8, 'This is consistent with the fact that the resources for scepticism and the resources for perceiving and encoding are in 36__________ in the brain.', 'C', 36);

-- D. Group 9 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37–40: Do the following statements agree with the claims of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 3);
SET @group9_id = LAST_INSERT_ID();
-- Use type_id 2 for 'Yes/No/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group9_id, 2, 'Campaigns designed to correct misinformation will fail to achieve their purpose if people are unable to understand them.', 'YES', 37),
(@group9_id, 2, 'Attempts to teach elementary school students about misinformation have been opposed.', 'NOT GIVEN', 38),
(@group9_id, 2, 'It may be possible to overcome the problem of misinformation in a relatively short period.', 'NO', 39),
(@group9_id, 2, 'The need to keep up with new information is hugely exaggerated in today’s world.', 'NOT GIVEN', 40);

-- -----------------------------------------------------------------
-- 1. INITIAL SETUP: Insert the Test record and capture its ID
-- -----------------------------------------------------------------
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 6 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 🏭 PART 2: PASSAGE 1: The Industrial Revolution in Britain (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The Industrial Revolution in Britain',
    'The Industrial Revolution began in Britain in the mid-1700s and by the 1830s and 1840s has spread to many other parts of the world, including the United States... [Full text inserted here]... By 1813, the Luddite resistance had all but vanished.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Notes Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1–7: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary/Notes Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'In Watt and Boulton’s steam engine, the movement of the 1__________ was linked to a gear system.', 'piston', 1),
(@group1_id, 8, 'A greater supply of 2__________ was required to power steam engines.', 'coal', 2),
(@group1_id, 8, 'Before the Industrial Revolution, spinners and weavers worked at home and in 3__________.', 'workshops', 3),
(@group1_id, 8, 'Not as much 4__________ was needed to produce cloth once the spinning jenny and power loom were invented.', 'labour', 4),
(@group1_id, 8, 'Smelting of iron ore with coke resulted in material that was better 5__________.', 'quality', 5),
(@group1_id, 8, 'Demand for iron increased with the growth of the 6__________.', 'railways', 6),
(@group1_id, 8, 'The new cities were dirty, crowded and lacked sufficient 7__________.', 'sanitation', 7);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8–13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'Britain’s canal network grew rapidly so that more goods could be transported around the country.', 'NOT GIVEN', 8),
(@group2_id, 1, 'Costs in the iron industry rose when the technique of smelting iron ore with coke was introduced.', 'FALSE', 9),
(@group2_id, 1, 'Samuel Morse’s communication system was more reliable than that developed by William Cooke and Charles Wheatstone.', 'NOT GIVEN', 10),
(@group2_id, 1, 'The economic benefits of industrialisation were limited to certain sectors of society.', 'TRUE', 11),
(@group2_id, 1, 'Some skilled weavers believed that the introduction of the new textile machines would lead to job losses.', 'TRUE', 12),
(@group2_id, 1, 'There was some sympathy among local people for the Luddites who were arrested near Huddersfield.', 'NOT GIVEN', 13);


-- -----------------------------------------------------------------
-- 🏃 PART 3: PASSAGE 2: Athletes and stress (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Athletes and stress',
    'It isn’t easy being a professional athlete. Not only are the physical demands greater than most people could handle, athletes also face intense psychological pressure during competition... [Full text inserted here]... This would increase the demands which players experience compared to a normal training session, while still allowing them to practise coping with stress.',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14–18: Reading Passage 2 has six paragraphs, A–F. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'reference to two chemical compounds which impact on performance', 'D', 14),
(@group3_id, 5, 'examples of strategies for minimising the effects of stress', 'F', 15),
(@group3_id, 5, 'how a sportsperson accounted for their own experience of stress', 'A', 16),
(@group3_id, 5, 'study results indicating links between stress responses and performance', 'C', 17),
(@group3_id, 5, 'mention of people who can influence how athletes perceive their stress responses', 'F', 18);

-- C. Group 4 (Sentence Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 19–22: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Sentence Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 8, 'Performance stress involves many demands on the athlete, for example, coping with the possible risk of 19__________.', 'injury', 19),
(@group4_id, 8, 'Cortisol can cause tennis players to produce fewer good 20__________.', 'serves', 20),
(@group4_id, 8, 'Psychologists can help athletes to view their physiological responses as the effect of a positive feeling such as 21__________.', 'excitement', 21),
(@group4_id, 8, '22__________ is an example of a psychological technique which can reduce an athlete’s stress responses.', 'Visualisation', 22);

-- D. Group 5 (Multiple Choice - Choose TWO - Emma Raducanu)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23 and 24: Choose TWO letters, A–E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO facts about Emma Raducanu’s withdrawal from the Wimbledon tournament are mentioned in the text?', '{"A": "the stage at which she dropped out of the tournament", "B": "symptoms of her performance stress at the tournament", "C": "measures which she had taken to manage her stress levels", "D": "aspects of the Wimbledon tournament which increased her stress levels", "E": "reactions to her social media posts about her experience at Wimbledon"}', 'B, D', 23);

-- E. Group 6 (Multiple Choice - Choose TWO - Anxiety Facts)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 25 and 26: Choose TWO letters, A–E.', 4);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group6_id, 3, 'Which TWO facts about anxiety are mentioned in Paragraph E of the text?', '{"A": "the factors which determine how severe it may be", "B": "how long it takes for its effects to become apparent", "C": "which of its symptoms is most frequently encountered", "D": "the types of athletes who are most likely to suffer from it", "E": "the harm that can result if athletes experience it too often"}', 'A, E', 25);


-- -----------------------------------------------------------------
-- 💡 PART 4: PASSAGE 3: An inquiry into the existence of the gifted child (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'An inquiry into the existence of the gifted child',
    'Let us start by looking at a modern ‘genius’, Maryam Mirzakhani, who died at the early age of 40... [Full text inserted here]... He once wrote: ‘It’s not that I’m so smart, it’s just that I stay with problems longer. Most people say it is the intellect which makes a great scientist. They are wrong: it is character.’',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 7 (Summary Completion - Matching Phrases)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27–32: Complete the summary using the list of phrases, A–K, below. A. appeal, B. determined, C. intrigued, D. single, E. achievement, F. devoted, G. involved, H. unique, I. innovative, J. satisfaction, K. intent', 1);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary Completion' (matching phrases)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 8, 'Maryam Mirzakhani is regarded as 27__________ in the field of mathematics because she was the only female holder of the prestigious Fields Medal – a record that she retained at the time of her death.', 'H', 27),
(@group7_id, 8, 'However, maths held little 28__________ for her as a child and in fact her performance was below average until she was 29__________ by a difficult puzzle that one of her siblings showed her.', 'A', 28),
(@group7_id, 8, 'However, maths held little 28__________ for her as a child and in fact her performance was below average until she was 29__________ by a difficult puzzle that one of her siblings showed her.', 'C', 29),
(@group7_id, 8, 'Later, as a professional mathematician, she had an inquiring mind and proved herself to be 30__________ when things did not go smoothly.', 'B', 30),
(@group7_id, 8, 'She said she got the greatest 31__________ from making ground-breaking discoveries and in fact she was responsible for some extremely 32__________ mathematical studies.', 'J', 31),
(@group7_id, 8, 'She said she got the greatest 31__________ from making ground-breaking discoveries and in fact she was responsible for some extremely 32__________ mathematical studies.', 'I', 32);

-- C. Group 8 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 33–37: Do the following statements agree with the claims of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 2);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 2 for 'Yes/No/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 2, 'Many people who ended up winning prestigious intellectual prizes only reached an average standard when young.', 'YES', 33),
(@group8_id, 2, 'Einstein’s failures as a young man were due to his lack of confidence.', 'NOT GIVEN', 34),
(@group8_id, 2, 'It is difficult to reach agreement on whether some children are actually born gifted.', 'YES', 35),
(@group8_id, 2, 'Einstein was upset by the public’s view of his life’s work.', 'NOT GIVEN', 36),
(@group8_id, 2, 'Einstein put his success down to the speed at which he dealt with scientific questions.', 'NO', 37);

-- D. Group 9 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 38–40: Choose the correct letter, A, B, C or D.', 3);
SET @group9_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group9_id, 3, 'What does Eyre believe is needed for children to equal ‘gifted’ standards?', '{"A": "strict discipline from the teaching staff", "B": "assistance from their peers in the classroom", "C": "the development of a spirit of inquiry towards their studies", "D": "the determination to surpass everyone else’s achievements"}', 'C', 38),
(@group9_id, 3, 'What is the result of Ericsson’s research?', '{"A": "Very gifted students do not need to work on improving memory skills.", "B": "Being born with a special gift is not the key factor in becoming expert.", "C": "Including time for physical exercise is crucial in raising performance.", "D": "10,000 hours of relevant and demanding work will create a genius."}', 'B', 39),
(@group9_id, 3, 'In the penultimate paragraph, it is stated the key to some deprived children’s success is', '{"A": "a regular and nourishing diet at home.", "B": "the loving support of more than one parent.", "C": "a community which has well-funded facilities for learning.", "D": "the guidance of someone who recognises the benefits of learning."}', 'D', 40);

-- -----------------------------------------------------------------
-- 1. INITIAL SETUP: Insert the Test record and capture its ID
-- -----------------------------------------------------------------
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 7 (P1-3)', 'Academic');
SET @test_id = LAST_INSERT_ID();


-- -----------------------------------------------------------------
-- 🏝️ PART 2: PASSAGE 1: Archaeologists discover evidence of prehistoric island settlers (Questions 1-13)
-- -----------------------------------------------------------------

-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Archaeologists discover evidence of prehistoric island settlers',
    'In early April 2019, Dr Ceri Shipton and his colleagues from Australian National University became the first archaeologists to explore Obi, one of many tropical islands in Indonesia’s Maluku Utara province... [Full text inserted here]... It seems likely, in view of Obi’s location, that this final phase of occupation also saw the Kelo shelters used by people involved in the historic trade in spices between the Maluku islands and the rest of the world.',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1–7: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 1);
SET @group1_id = LAST_INSERT_ID();
-- Use type_id 1 for 'True/False/Not Given'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 1, 'Archaeological research had taken place on the island of Obi before the arrival of Ceri Shipton and his colleagues.', 'FALSE', 1),
(@group1_id, 1, 'At the Kelo sites, the researchers found the first clam shell axes ever to be discovered in the region.', 'FALSE', 2),
(@group1_id, 1, 'The size of Obi today is less than it was 18,000 years ago.', 'TRUE', 3),
(@group1_id, 1, 'A change in the climate around 11,700 years ago had a greater impact on Obi than on the surrounding islands.', 'NOT GIVEN', 4),
(@group1_id, 1, 'The researchers believe there is a connection between warmer, wetter weather and a change in the material used to make axes.', 'TRUE', 5),
(@group1_id, 1, 'Shipton’s team were surprised to find evidence of the Obi islanders’ hunting practices.', 'NOT GIVEN', 6),
(@group1_id, 1, 'It is thought that the Kelo shelters were occupied continuously until about 1,000 years ago.', 'FALSE', 7);

-- C. Group 2 (Notes Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8–13: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group2_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Summary/Notes Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 8, 'Excavations of rock shelters inside 8__________ near the village of Kelo revealed:', 'caves', 8),
(@group2_id, 8, 'axes made out of 9__________ , dating from around 11,700 years ago', 'stone', 9),
(@group2_id, 8, '10__________ of an animal: evidence of what ancient islanders ate', 'bones', 10),
(@group2_id, 8, '11__________ which resembled ones found on other islands.', 'beads', 11),
(@group2_id, 8, 'had 12__________ as well as items made out of metal', 'pottery', 12),
(@group2_id, 8, 'probably took part in the production and sale of 13__________ .', 'spices', 13);


-- -----------------------------------------------------------------
-- 💧 PART 3: PASSAGE 2: The global importance of wetlands (Questions 14-26)
-- -----------------------------------------------------------------

-- A. Insert Passage 2
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The global importance of wetlands',
    'Wetlands are areas where water covers the soil, or is present either at or near the surface of the soil, for all or part of the year... [Full text inserted here]... ‘Awareness of the importance of wetlands is growing,’ he says. ‘It’s true that wetland degradation still continues at a rapid pace, but my impression is that things are slowly changing.’',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14–17: Reading Passage 2 has eight paragraphs, A–H. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Use type_id 5 for 'Matching Information'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'reference to the need to ensure that inhabitants of wetland regions continue to benefit from them', 'G', 14),
(@group3_id, 5, 'the proportion of wetlands which have already been lost', 'A', 15),
(@group3_id, 5, 'reference to the idea that people are beginning to appreciate the value of wetlands', 'H', 16),
(@group3_id, 5, 'mention of the cultural significance of wetlands', 'B', 17);

-- C. Group 4 (Sentence Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 18–22: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Use type_id 8 for 'Sentence Completion'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 8, 'Peatlands which have been drained begin to release 18__________ instead of storing it.', 'carbon', 18),
(@group4_id, 8, 'Once peatland areas have been cleared, 19__________ are more likely to occur.', 'fires', 19),
(@group4_id, 8, 'Clearing peatland forests to make way for oil palm plantations destroys the 20__________ of the local environment.', 'biodiversity', 20),
(@group4_id, 8, 'Water is drained out of peatlands through the 21__________ which are created by logging companies.', 'ditches', 21),
(@group4_id, 8, 'Draining peatlands leads to 22__________ : a serious problem which can eventually result in coastal flooding and land loss.', 'subsidence', 22);

-- D. Group 5 (Matching Features - Experts)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23–26: Look at the following statements (Questions 23–26) and the list of experts below. Match each statement with the correct expert, A–D. List of Experts: A. Matthew McCartney, B. Pieter van Eijk, C. Marcel Silvius, D. Dave Tickner', 3);
SET @group5_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group5_id, 6, 'Communities living in wetland regions must be included in discussions about the future of these areas.', 'A', 23),
(@group5_id, 6, 'Official policies towards wetlands vary from one nation to the next.', 'C', 24),
(@group5_id, 6, 'People cause harm to wetlands without having any intention to do so.', 'D', 25),
(@group5_id, 6, 'Initiatives to reserve environmental damage need to be complex.', 'B', 26);


-- -----------------------------------------------------------------
-- 🗣️ PART 4: PASSAGE 3: Is the era of artificial speech translation upon us? (Questions 27-40)
-- -----------------------------------------------------------------

-- A. Insert Passage 3
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Is the era of artificial speech translation upon us?',
    'Noise, Alex Waibel tells me, is one of the major challenges that artificial speech translation has to meet... [Full text inserted here]... Though the practical need for a common language will diminish, the social value of sharing one will persist. And software will never be a substitute for the subtle but vital understanding that comes with knowledge of a language.',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27–30: Choose the correct letter, A, B, C or D.', 1);
SET @group6_id = LAST_INSERT_ID();
-- Use type_id 3 for 'Multiple Choice'
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group6_id, 3, 'What does the reader learn about the conversation in the first paragraph?', '{"A": "The speakers are communicating in different languages.", "B": "Neither of the speakers is familiar with their environment.", "C": "The topic of the conversation is difficult for both speakers.", "D": "Aspects of the conversation are challenging for both speakers."}', 'D', 27),
(@group6_id, 3, 'What assists the electronic translator during lectures at Karlsruhe Institute of Technology?', '{"A": "the repeated content of lectures", "B": "the students’ reading skills", "C": "the languages used", "D": "the lecturers’ technical ability"}', 'A', 28),
(@group6_id, 3, 'When referring to The Hitchhiker’s Guide to the Galaxy, the writer suggests that', '{"A": "the Babel fish was considered undesirable at the time.", "B": "this book was not seriously intending to predict the future.", "C": "artificial speech translation was not a surprising development.", "D": "some speech translation techniques are better than others."}', 'C', 29),
(@group6_id, 3, 'What does the writer say about sharing earpieces?', '{"A": "It is something people will get used to doing.", "B": "The reluctance to do this is understandable.", "C": "The equipment will be unnecessary in the future.", "D": "It is something few people need to worry about."}', 'B', 30);

-- C. Group 7 (Matching Features - Sentence Endings)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 31–34: Complete each sentence with the correct ending, A–F, below. A. but there are concerns about this. B. as systems do not need to conform to standard practices. C. but they are far from perfect. D. despite the noise issues. E. because translation is immediate. F. and have an awareness of good manners.', 2);
SET @group7_id = LAST_INSERT_ID();
-- Use type_id 6 for 'Matching Features'
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'Speech translation methods are developing fast in Japan', 'C', 31),
(@group7_id, 6, 'TV interviews that use translation voiceover methods are successful', 'E', 32),
(@group7_id, 6, 'Future translation systems should address people appropriately', 'F', 33),
(@group7_id, 6, 'Users may be able to maintain their local customs', 'B', 34);

-- D. Group 8 (Yes/No/Not Given - Writer''s Views)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 35–40: Do the following statements agree with the views of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 3);
SET @group8_id = LAST_INSERT_ID();
-- Use type_id 2 for 'Yes/No/Not Given' (claims of the writer)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 2, 'Language translation systems will be seen as very useful throughout the academic and professional worlds.', 'NO', 35),
(@group8_id, 2, 'The overall value of automated translation to family life is yet to be shown.', 'YES', 36),
(@group8_id, 2, 'Automated translation could make life more difficult for immigrant families.', 'NO', 37),
(@group8_id, 2, 'Visual aspects of language translation are being considered by scientists.', 'NOT GIVEN', 38),
(@group8_id, 2, 'International scientists have found English easier to translate into other languages than Latin.', 'NOT GIVEN', 39),
(@group8_id, 2, 'As far as language is concerned, there is a difference between people’s social and practical needs.', 'YES', 40);



