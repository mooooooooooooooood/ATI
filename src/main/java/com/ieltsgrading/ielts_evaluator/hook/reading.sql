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

-- CORRECTED BLOCK TO USE FOR KĀKĀPŌ PASSAGE 1
-- A. Insert Passage 1
-- A. Insert Passage 1
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The kākāpō',
    '<p>The kākāpō is a nocturnal, flightless parrot that is critically endangered and one of New Nealand''s unique treasures. The kākāpō, also known as the owl parrot, is a large, forest-dwelling bird, with a pale owl-like face. Up to 64 cm in length, it has predominantly yellow-green feathers, forward-facing eyes, a large grey beak, large blue feet, and relatively short wings and tail. It is the world''s only flightless parrot, and is also possibly one of the world''s longest-living birds, with a reported lifespan of up to 100 years.</p><p>Kākāpō are solitary birds and tend to occupy the same home range for many years. They forage on the ground and climb high into trees. They often leap from trees and flap their wings, but at best manage a controlled descent to the ground. They are entirely vegetarian, with their diet including the leaves, roots and bark of trees as well as bulbs, and fern fronds.</p><p>Kākāpō breed in summer and autumn, but only in years when food is plentiful. Males play no part in incubation or chick-rearing - females alone incubate eggs and feed the chicks. The 1-4 eggs are laid in soil, which is repeatedly turned over before and during incubation. The female kākāpō has to spend long periods away from the nest searching for food, which leaves the unattended eggs and chicks particularly vulnerable to predators.</p><p>Before humans arrived, kākāpō were common throughout New Zealand''s forests. However, this all changed with the arrival of the first Polynesian settlers about 700 years ago. For the early settlers, the flightless kākāpō was easy prey. They ate its meat and used its feathers to make soft cloaks. With them came the Polynesian dog and rat, which also preyed on kākāpō. By the time European colonisers arrived in the early 1800s, kākāpō had become confined to the central North Island and forested parts of the South Island. The fall in kākāpō numbers was accelerated by European colonisation. A great deal of habitat was lost through forest clearance, and introduced species such as deer depleted the remaining forests of food. Other predators such as cats, stoats and two more species of rat were also introduced. The kākāpō were in serious trouble.</p><p>In 1894, the New Zealand government launched its first attempt to save the kākāpō. Conservationist Richard Henry led an effort to relocate several hundred of the birds to predator-free Resolution Island in Fiordland. Unfortunately, the island didn''t remain predator free - stoats arrived within six years, eventually destroying the kākāpō population. By the mid-1900s, the kākāpō was practically a lost species. Only a few clung to life in the most isolated parts of New Zealand.</p><p>From 1949 to 1973, the newly formed New Zealand Wildlife Service made over 60 expeditions to find kākāpō, focusing mainly on Fiordland. Six were caught, but there were no females amongst them and all but one died within a few months of captivity. In 1974, a new initiative was launched, and by 1977, 18 more kākāpō were found in Fiordland. However, there were still no females. In 1977, a large population of males was spotted in Rakiura - a large island free from stoats, ferrets and weasels. There were about 200 individuals, and in 1980 it was confirmed females were also present. These birds have been the foundation of all subsequent work in managing the species.</p><p>Unfortunately, predation by feral cats on Rakiura Island led to a rapid decline in kākāpō numbers. As a result, during 1980-97, the surviving population was evacuated to three island sanctuaries: Codfish Island, Maud Island and Little Barrier Island. However, breeding success was hard to achieve. Rats were found to be a major predator of kākāpō chicks and an insufficient number of chicks survived to offset adult mortality. By 1995, although at least 12 chicks had been produced on the islands, only three had survived. The kākāpō population had dropped to 51 birds. The critical situation prompted an urgent review of kākāpō management in New Zealand.</p><p>In 1996, a new Recovery Plan was launched, together with a specialist advisory group called the Kākāpō Scientific and Technical Advisory Committee and a higher amount of funding. Renewed steps were taken to control predators on the three islands. Cats were eradicated from Little Barrier Island in 1980, and possums were eradicated from Codfish Island by 1986. However, the population did not start to increase until rats were removed from all three islands, and the birds were more intensively managed. This involved moving the birds between islands, supplementary feeding of adults and rescuing and hand-raising any failing chicks.</p><p>After the first five years of the Recovery Plan, the population was on target. By 2000, five new females had been produced, and the total population had grown to 62 birds. For the first time, there was cautious optimism for the future of kākāpō and by June 2020, a total of 210 birds was recorded.</p><p>Today, kākāpō management continues to be guided by the kākāpō Recovery Plan. Its key goals are: minimise the loss of genetic diversity in the kākāpō population, restore or maintain sufficient habitat to accommodate the expected increase in the kākāpō population, and ensure stakeholders continue to be fully engaged in the preservation of the species.</p>',
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
    'Procrastination',
    '<p>A psychologist explains why we put off important tasks and how we can break this habit</p><p>A. Procrastination is the habit of delaying a necessary task, usually by focusing on less urgent, more enjoyable, and easier activities instead. We all do it from time to time. We might be composing a message to a friend who we have to let down, or putting together an important report for college or work; we''re doing our best to avoid doing the job at hand, but deep down we know that we should just be getting on with it. Unfortunately, berating ourselves won''t stop us procrastinating again. In fact, it''s one of the worst things we can do. This matters because, as my research shows, procrastination doesn''t just waste time, but is actually linked to other problems, too.</p><p>B. Contrary to popular belief, procrastination is not due to laziness or poor time management. Scientific studies suggest procrastination is, in fact, caused by poor mood management. This makes sense if we consider that people are more likely to put off starting or completing tasks that they are really not keen to do. If just thinking about the task threatens our sense of self-worth or makes us anxious, we will be more likely to put it off. Research involving brain imaging has found that areas of the brain linked to detection of threats and emotion regulation are actually different in people who chronically procrastinate compared to those who don''t procrastinate frequently.</p><p>C Tasks that are emotionally loaded or difficult, such as preparing for exams, are prime candidates for procrastination. People with low self-esteem are more likely to procrastinate. Another group of people who tend to procrastinate are perfectionists, who worry their work will be judged harshly by others. We know that if we don''t finish that report or complete those home repairs, then what we did can''t be evaluated. When we avoid such tasks, we also avoid the negative emotions associated with them. This is rewarding, and it conditions us to use procrastination to repair our mood. If we engage in more enjoyable tasks instead, we get another mood boost. In the long run, however, procrastination isn''t an effective way of managing emotions. The ''mood repair'' we experience is temporary. Afterwards, people tend to be left with a sense of guilt that not only increases their negative mood, but also reinforces their tendency to procrastinate.</p><p>D. So why is this such a problem? When most people think of the costs of procrastination, they think of the toll on productivity. For example, studies have shown that procrastination negatively impacts on student performance. But putting off reading textbooks and writing essays may affect other areas of students'' lives. In one study of over 3,000 German students over a six-month period, those who reported procrastinating over their university work were also more likely to engage in study-related misconduct, such as cheating and plagiarism. But the behaviour that procrastination was most closely linked with was using fraudulent excuses to get deadline extensions. Other research shows that employees on average spend almost a quarter of their workday procrastinating, and again this is linked with negative outcomes. In fact, in one US survey of over 22,000 employees, participants who said they regularly procrastinated had less annual income and less employment stability. For every one-point increase on a measure of chronic procrastination, annual income decreased by US$15,000.</p><p>E. Procrastination also correlates with serious health and well-being problems. A tendency to procrastinate is linked to poor mental health, including higher levels of depression and anxiety. Across numerous studies, I''ve found people who regularly procrastinate report a greater number of health issues, such as headaches, flu and colds, and digestive issues. They also experience higher levels of stress and poor sleep quality. They are less likely to practise healthy behaviours, such as eating a healthy diet and regularly exercising, and use destructive coping strategies to manage their stress. In one study of over 700 people, I found people prone to procrastination had a 63% greater risk of poor heart health after accounting for other personality traits and demographics.</p><p>F. Finding better ways of managing our emotions is one route out of the vicious cycle of procrastination. An important first step is to manage our environment and how we view the task. There are a number of evidence-based strategies that can help us fend off distractions that can occupy our minds when we should be focusing on the thing we should be getting on with. For example, reminding ourselves about why the task is important and valuable can increase positive feelings towards it. Forgiving ourselves and feeling compassion when we procrastinate can help break the procrastination cycle. We should admit that we feel bad, but not be overly critical of ourselves. We should remind ourselves that we''re not the first person to procrastinate, nor the last. Doing this can take the edge off the negative feelings we have about ourselves when we procrastinate. This can all make it easier to get back on track.</p>',
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
    'Invasion of the Robot Umpires',
    '<p>A few years ago, Fred DeJesus from Brooklyn, New York became the first umpire in a minor league baseball game to use something called the Automated Ball-Strike System (ABS), often referred to as the ''robo-umpire''. Instead of making any judgments himself about a strike, DeJesus had decisions fed to him through an earpiece, connected to a modified missile-tracking system. The contraption looked like a large black pizza box with one glowing green eye; it was mounted above the press stand.</p><p>Major League Baseball (MLB), who had commissioned the system, wanted human umpires to announce the calls, just as they would have done in the past. When the first pitch came in, a recorded voice told DeJesus it was a strike. Previously, calling a strike was a judgment call on the part of the umpire. Even if the batter does not hit the ball, a pitch that passes through the ''strike zone'' (an imaginary zone about seventeen inches wide, stretching from the batter''s knees to the middle of his chest) is considered a strike. During that first game, when DeJesus announced calls, there was no heckling and no shouted disagreement. Nobody said a word.</p><p>For a hundred and fifty years or so, the strike zone has been the game''s animating force—countless arguments between a team''s manager and the umpire have taken place over its boundaries and whether a ball had crossed through it. The rules of play have evolved in various stages. Today, everyone knows that you may scream your disagreement in an umpire''s face, but you must never shout personal abuse at them or touch them. That''s a no-no. When the robo-umpires came, however, the arguments stopped.</p><p>During the first robo-umpire season, players complained about some strange calls. In response, MLB decided to tweak the dimensions of the zone, and the following year the consensus was that ABS is profoundly consistent. MLB says the device is near-perfect, precise to within fractions of an inch. ''It''ll reduce controversy in the game, and be good for the game,'' says Rob Manfred, who is Commissioner for MLB. But the question is whether controversy is worth reducing, or whether it is the sign of a human hand.</p><p>A human, at least, yells back. When I spoke with Frank Viola, a coach for a North Carolina team, he said that ABS works as designed, but that it was also unforgiving and pedantic, almost legalistic. ''Manfred is a lawyer,'' Viola noted. Some pitchers have complained that, compared with a human''s, the robot''s strike zone seems too precise. Viola was once a major-league player himself. When he was pitching, he explained, umpires rewarded skill. ''Throw it where you aimed, and it would be a strike, even if it was an inch or two outside. There was a dialogue between pitcher and umpire.''</p><p>The executive tasked with running the experiment for MLB is Morgan Sword, who''s in charge of baseball operations. According to Sword, ABS was part of a larger project to make baseball more exciting since executives are terrified of losing younger fans, as has been the case with horse racing and boxing. He explains how they began the process by asking fans what version of baseball they found most exciting. The results showed that everyone wanted more action: more hits, more defense, more baserunning. This type of baseball essentially hasn''t existed since the 1960s, when the hundred-mile-an-hour fastball, which is difficult to hit and control, entered the game. It flattened the game into strikeouts, walks, and home runs—a type of play lacking much action.</p><p>Sword''s team brainstormed potential fixes. Any rule that existed, they talked about changing—from changing the bats to changing the geometry of the field. But while all of these were ruled out as potential fixes, ABS was seen as a perfect vehicle for change. According to Sword, once you get the technology right, you can load any strike zone you want into the system. ''It might be a triangle, or a blob, or something shaped like Texas. Over time, as baseball evolves, ABS can allow the zone to change with it.''</p><p>''In the past twenty years, sports have moved away from judgment calls. Soccer has Video Assistant Referees (for offside decisions, for example). Tennis has Hawk-Eye da chon 1 muc (for line calls, for example). For almost a decade, baseball has used instant replay on the base paths. This is widely liked, even if the precision can sometimes cause problems. But these applications deal with something physical: bases, lines, goals. The boundaries of action are precise, delineated like the keys of a piano. This is not the case with ABS and the strike zone. Historically, a certain discretion has been appreciated.''</p><p>I decided to email Alva Noë, a professor at Berkeley University and a baseball fan, for his opinion. ''Hardly a day goes by that I don''t wake up and run through the reasons that this [robo-umpires] is such a terrible idea,'' he replied. He later told me, ''This is part of a movement to use algorithms to take the hard choices of living out of life.'' Perhaps he''s right. We watch baseball to kill time, not to maximize it. Some players I have met take a dissenting stance toward the robots too, believing that accuracy is not the answer. According to Joe Russo, who plays for a New Jersey team, ''With technology, people just want everything to be perfect. That''s not reality. I think perfect would be weird. Your teams are always winning, work is always just great, there''s always money in your pocket, your car never breaks down. What is there to talk about?''</p><p>*strike: a strike is when the batter swings at a ball and misses or when the batter does not swing at a ball that passes through the strike zone.</p>',
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

--
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



-- -----------------------------------------------------------------
-- 📝 FULL TEST IMPORT: Manatees / Procrastination / Robot Umpires
-- -----------------------------------------------------------------

USE ielts_db;

-- 1. Create the Test Record
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 3 (Manatees/Procrastination/Robots)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- =================================================================
-- 🌊 PASSAGE 1: MANATEES
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Manatees',
    '<p>Manatees, also known as sea cows, are aquatic mammals that belong to a group of animals called Sirenia. This group also contains dugongs. Dugongs and manatees look quite alike - they are similar in size, colour and shape, and both have flexible flippers for forelimbs. However, the manatee has a broad, rounded tail, whereas the dugong''s is fluked, like that of a whale. There are three species of manatees: the West Indian manatee (Trichechus manatus), the African manatee (Trichechus senegalensis) and the Amazonian manatee (Trichechus inunguis).</p><p>Unlike most mammals, manatees have only six bones in their neck - most others, including humans and giraffes, have seven. This short neck allows a manatee to move its head up and down, but not side to side. To see something on its left or its right, a manatee must turn its entire body, steering with its flippers. Manatees have pectoral flippers but no back limbs, only a tail for propulsion. They do have pelvic bones, however–a leftover from their evolution from a four-legged to a fully aquatic animal. Manatees share some visual similarities to elephants. Like elephants, manatees have thick, wrinkled skin. They also have some hairs covering their bodies which help them sense vibrations in the water around them.</p><p>Seagrasses and other marine plants make up most of a manatee''s diet. Manatees spend about eight hours each day grazing and uprooting plants. They eat up to 15% of their weight in food each day. African manatees are omnivorous - studies have shown that molluscs and fish make up a small part of their diets. West Indian and Amazonian manatees are both herbivores.</p><p>Manatees'' teeth are all molars - flat, rounded teeth for grinding food. Due to manatees'' abrasive aquatic plant diet, these teeth get worn down and they eventually fall out, so they continually grow new teeth that get pushed forward to replace the ones they lose. Instead of having incisors to grasp their food, manatees have lips which function like a pair of hands to help tear food away from the seafloor.</p><p>Manatees are fully aquatic, but as mammals, they need to come up to the surface to breathe. When awake, they typically surface every two to four minutes, but they can hold their breath for much longer. Adult manatees sleep underwater for 10-12 hours a day, but they come up for air every 15-20 minutes. Active manatees need to breathe more frequently. It''s thought that manatees use their muscular diaphragm and breathing to adjust their buoyancy. They may use diaphragm contractions to compress and store gas in folds in their large intestine to help them float.</p><p>The West Indian manatee reaches about 3.5 metres long and weighs on average around 500 kilogrammes. It moves between fresh water and salt water, taking advantage of coastal mangroves and coral reefs, rivers, lakes and inland lagoons. There are two subspecies of West Indian manatee: the Antillean manatee is found in waters from the Bahamas to Brazil, whereas the Florida manatee is found in US waters, although some individuals have been recorded in the Bahamas. In winter, the Florida manatee is typically restricted to Florida. When the ambient water temperature drops below 20°C, it takes refuge in naturally and artificially warmed water, such as at the warm-water outfalls from powerplants.</p><p>The African manatee is also about 3.5 metres long and found in the sea along the west coast of Africa, from Mauritania down to Angola. The species also makes use of rivers, with the mammals seen in landlocked countries such as Mali and Niger. The Amazonian manatee is the smallest species, though it is still a big animal. It grows to about 2.5 metres long and 350 kilogrammes. Amazonian manatees favour calm, shallow waters that are above 23°C. This species is found in fresh water in the Amazon Basin in Brazil, as well as in Colombia, Ecuador and Peru.</p><p>All three manatee species are endangered or at a heightened risk of extinction. The African manatee and Amazonian manatee are both listed as Vulnerable by the International Union for Conservation of Nature (IUCN). It is estimated that 140,000 Amazonian manatees were killed between 1935 and 1954 for their meat, fat and skin with the latter used to make leather. In more recent years, African manatee decline has been tied to incidental capture in fishing nets and hunting. Manatee hunting is now illegal in every country the African species is found in.</p><p>The two subspecies of West Indian manatee are listed as Endangered by the IUCN. Both are also expected to undergo a decline of 20% over the next 40 years. A review of almost 1,800 cases of entanglement in fishing nets and of plastic consumption among marine mammals in US waters from 2009 to 2020 found that at least 700 cases involved manatees. The chief cause of death in Florida manatees is boat strikes. However, laws in certain parts of Florida now limit boat speeds during winter, allowing slow-moving manatees more time to respond.</p>',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-6: Complete the notes below. Choose ONE WORD AND/OR A NUMBER from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
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
-- Type 1: True/False/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'West Indian manatees can be found in a variety of different aquatic habitats.', 'TRUE', 7),
(@group2_id, 1, 'The Florida manatee lives in warmer waters than the Antillean manatee.', 'NOT GIVEN', 8),
(@group2_id, 1, 'The African manatee''s range is limited to coastal waters between the West African countries of Mauritania and Angola.', 'FALSE', 9),
(@group2_id, 1, 'The extent of the loss of Amazonian manatees in the mid-twentieth century was only revealed many years later.', 'NOT GIVEN', 10),
(@group2_id, 1, 'It is predicted that West Indian manatee populations will fall in the coming decades.', 'TRUE', 11),
(@group2_id, 1, 'The risk to manatees from entanglement and plastic consumption increased significantly in the period 2009-2020.', 'NOT GIVEN', 12),
(@group2_id, 1, 'There is some legislation in place which aims to reduce the likelihood of boat strikes on manatees in Florida.', 'TRUE', 13);


-- =================================================================
-- 🕰️ PASSAGE 2: PROCRASTINATION
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Procrastination',
    '<p>A psychologist explains why we put off important tasks and how we can break this habit</p><p>A. Procrastination is the habit of delaying a necessary task, usually by focusing on less urgent, more enjoyable, and easier activities instead. We all do it from time to time. We might be composing a message to a friend who we have to let down, or putting together an important report for college or work; we''re doing our best to avoid doing the job at hand, but deep down we know that we should just be getting on with it. Unfortunately, berating ourselves won''t stop us procrastinating again. In fact, it''s one of the worst things we can do. This matters because, as my research shows, procrastination doesn''t just waste time, but is actually linked to other problems, too.</p><p>B. Contrary to popular belief, procrastination is not due to laziness or poor time management. Scientific studies suggest procrastination is, in fact, caused by poor mood management. This makes sense if we consider that people are more likely to put off starting or completing tasks that they are really not keen to do. If just thinking about the task threatens our sense of self-worth or makes us anxious, we will be more likely to put it off. Research involving brain imaging has found that areas of the brain linked to detection of threats and emotion regulation are actually different in people who chronically procrastinate compared to those who don''t procrastinate frequently.</p><p>C. Tasks that are emotionally loaded or difficult, such as preparing for exams, are prime candidates for procrastination. People with low self-esteem are more likely to procrastinate. Another group of people who tend to procrastinate are perfectionists, who worry their work will be judged harshly by others. We know that if we don''t finish that report or complete those home repairs, then what we did can''t be evaluated. When we avoid such tasks, we also avoid the negative emotions associated with them. This is rewarding, and it conditions us to use procrastination to repair our mood. If we engage in more enjoyable tasks instead, we get another mood boost. In the long run, however, procrastination isn''t an effective way of managing emotions. The ''mood repair'' we experience is temporary. Afterwards, people tend to be left with a sense of guilt that not only increases their negative mood, but also reinforces their tendency to procrastinate.</p><p>D. So why is this such a problem? When most people think of the costs of procrastination, they think of the toll on productivity. For example, studies have shown that procrastination negatively impacts on student performance. But putting off reading textbooks and writing essays may affect other areas of students'' lives. In one study of over 3,000 German students over a six-month period, those who reported procrastinating over their university work were also more likely to engage in study-related misconduct, such as cheating and plagiarism. But the behaviour that procrastination was most closely linked with was using fraudulent excuses to get deadline extensions. Other research shows that employees on average spend almost a quarter of their workday procrastinating, and again this is linked with negative outcomes. In fact, in one US survey of over 22,000 employees, participants who said they regularly procrastinated had less annual income and less employment stability. For every one-point increase on a measure of chronic procrastination, annual income decreased by US$15,000.</p><p>E. Procrastination also correlates with serious health and well-being problems. A tendency to procrastinate is linked to poor mental health, including higher levels of depression and anxiety. Across numerous studies, I''ve found people who regularly procrastinate report a greater number of health issues, such as headaches, flu and colds, and digestive issues. They also experience higher levels of stress and poor sleep quality. They are less likely to practise healthy behaviours, such as eating a healthy diet and regularly exercising, and use destructive coping strategies to manage their stress. In one study of over 700 people, I found people prone to procrastination had a 63% greater risk of poor heart health after accounting for other personality traits and demographics.</p><p>F. Finding better ways of managing our emotions is one route out of the vicious cycle of procrastination. An important first step is to manage our environment and how we view the task. There are a number of evidence-based strategies that can help us fend off distractions that can occupy our minds when we should be focusing on the thing we should be getting on with. For example, reminding ourselves about why the task is important and valuable can increase positive feelings towards it. Forgiving ourselves and feeling compassion when we procrastinate can help break the procrastination cycle. We should admit that we feel bad, but not be overly critical of ourselves. We should remind ourselves that we''re not the first person to procrastinate, nor the last. Doing this can take the edge off the negative feelings we have about ourselves when we procrastinate. This can all make it easier to get back on track.</p>',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-16: Reading Passage 2 has six paragraphs, A-F. Which paragraph contains the following information? NB You may use any letter more than once.', 1);
SET @group3_id = LAST_INSERT_ID();
-- Type 5: Matching Information
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'mention of false assumptions about why people procrastinate', 'B', 14),
(@group3_id, 5, 'reference to the realisation that others also procrastinate', 'F', 15),
(@group3_id, 5, 'neurological evidence of a link between procrastination and emotion', 'B', 16);

-- C. Group 4 (Summary Completion - One Word Only)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 17-22: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 8, 'Many people think that procrastination is the result of 17__________.', 'laziness', 17),
(@group4_id, 8, '...cause us to feel 18__________ when we think about them.', 'anxious', 18),
(@group4_id, 8, '...identifying 19__________.', 'threats', 19),
(@group4_id, 8, 'Getting ready to take 20__________ might be a typical example of one such task.', 'exams', 20),
(@group4_id, 8, 'People who are likely to procrastinate tend to be either 21__________ those with low self-esteem.', 'perfectionists', 21),
(@group4_id, 8, 'It''s often followed by a feeling of 22__________, which worsens our mood and leads to more procrastination.', 'guilt', 22);

-- D. Group 5 (Multiple Choice - Multi Select)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23-26: Choose TWO letters, A-E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
-- NOTE: We insert these as individual items. The application logic handles the "Choose 2" scoring.
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO comparisons between employees who often procrastinate and those who do not are mentioned in the text?', '{"A": "Their salaries are lower.", "B": "The quality of their work is inferior.", "C": "They don''t keep their jobs for as long.", "D": "They don''t enjoy their working lives as much.", "E": "They have poorer relationships with colleagues."}', 'A, C', 23),
(@group5_id, 3, 'Which TWO recommendations for getting out of a cycle of procrastination does the writer give?', '{"A": "not judging ourselves harshly", "B": "setting ourselves manageable aims", "C": "rewarding ourselves for tasks achieved", "D": "prioritising tasks according to their importance", "E": "avoiding things that stop us concentrating on our tasks"}', 'A, E', 25);
-- Note: Usually Q23-24 are one block, Q25-26 are one block. I used 23 and 25 as start indices here.


-- =================================================================
-- 🤖 PASSAGE 3: INVASION OF THE ROBOT UMPIRES
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Invasion of the Robot Umpires',
    '<p>A few years ago, Fred DeJesus from Brooklyn, New York became the first umpire in a minor league baseball game to use something called the Automated Ball-Strike System (ABS), often referred to as the ''robo-umpire''. Instead of making any judgments himself about a strike*, DeJesus had decisions fed to him through an earpiece, connected to a modified missile-tracking system. The contraption looked like a large black pizza box with one glowing green eye; it was mounted above the press stand.</p><p>Major League Baseball (MLB), who had commissioned the system, wanted human umpires to announce the calls, just as they would have done in the past. When the first pitch came in, a recorded voice told DeJesus it was a strike. Previously, calling a strike was a judgment call on the part of the umpire. Even if the batter does not hit the ball, a pitch that passes through the ''strike zone'' (an imaginary zone about seventeen inches wide, stretching from the batter''s knees to the middle of his chest) is considered a strike. During that first game, when DeJesus announced calls, there was no heckling and no shouted disagreement. Nobody said a word.</p><p>For a hundred and fifty years or so, the strike zone has been the game''s animating force—countless arguments between a team''s manager and the umpire have taken place over its boundaries and whether a ball had crossed through it. The rules of play have evolved in various stages. Today, everyone knows that you may scream your disagreement in an umpire''s face, but you must never shout personal abuse at them or touch them. That''s a no-no. When the robo-umpires came, however, the arguments stopped.</p><p>During the first robo-umpire season, players complained about some strange calls. In response, MLB decided to tweak the dimensions of the zone, and the following year the consensus was that ABS is profoundly consistent. MLB says the device is near-perfect, precise to within fractions of an inch. ''It''ll reduce controversy in the game, and be good for the game,'' says Rob Manfred, who is Commissioner for MLB. But the question is whether controversy is worth reducing, or whether it is the sign of a human hand.</p><p>A human, at least, yells back. When I spoke with Frank Viola, a coach for a North Carolina team, he said that ABS works as designed, but that it was also unforgiving and pedantic, almost legalistic. ''Manfred is a lawyer,'' Viola noted. Some pitchers have complained that, compared with a human''s, the robot''s strike zone seems too precise. Viola was once a major-league player himself. When he was pitching, he explained, umpires rewarded skill. ''Throw it where you aimed, and it would be a strike, even if it was an inch or two outside. There was a dialogue between pitcher and umpire.''</p><p>The executive tasked with running the experiment for MLB is Morgan Sword, who''s in charge of baseball operations. According to Sword, ABS was part of a larger project to make baseball more exciting since executives are terrified of losing younger fans, as has been the case with horse racing and boxing. He explains how they began the process by asking fans what version of baseball they found most exciting. The results showed that everyone wanted more action: more hits, more defense, more baserunning. This type of baseball essentially hasn''t existed since the 1960s, when the hundred-mile-an-hour fastball, which is difficult to hit and control, entered the game. It flattened the game into strikeouts, walks, and home runs—a type of play lacking much action.</p><p>Sword''s team brainstormed potential fixes. Any rule that existed, they talked about changing—from changing the bats to changing the geometry of the field. But while all of these were ruled out as potential fixes, ABS was seen as a perfect vehicle for change. According to Sword, once you get the technology right, you can load any strike zone you want into the system. ''It might be a triangle, or a blob, or something shaped like Texas. Over time, as baseball evolves, ABS can allow the zone to change with it.''</p><p>''In the past twenty years, sports have moved away from judgment calls. Soccer has Video Assistant Referees (for offside decisions, for example). Tennis has Hawk-Eye da chon 1 muc (for line calls, for example). For almost a decade, baseball has used instant replay on the base paths. This is widely liked, even if the precision can sometimes cause problems. But these applications deal with something physical: bases, lines, goals. The boundaries of action are precise, delineated like the keys of a piano. This is not the case with ABS and the strike zone. Historically, a certain discretion has been appreciated.''</p><p>I decided to email Alva Noë, a professor at Berkeley University and a baseball fan, for his opinion. ''Hardly a day goes by that I don''t wake up and run through the reasons that this [robo-umpires] is such a terrible idea,'' he replied. He later told me, ''This is part of a movement to use algorithms to take the hard choices of living out of life.'' Perhaps he''s right. We watch baseball to kill time, not to maximize it. Some players I have met take a dissenting stance toward the robots too, believing that accuracy is not the answer. According to Joe Russo, who plays for a New Jersey team, ''With technology, people just want everything to be perfect. That''s not reality. I think perfect would be weird. Your teams are always winning, work is always just great, there''s always money in your pocket, your car never breaks down. What is there to talk about?''</p><p>*strike: a strike is when the batter swings at a ball and misses or when the batter does not swing at a ball that passes through the strike zone.</p>',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-32: Do the following statements agree with the claims of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 1);
SET @group6_id = LAST_INSERT_ID();
-- Type 2: Yes/No/Not Given
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
-- Type 8 (Summary) or Type 6 (Features) - Using the Option Letter as Correct Answer
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'Even after ABS was developed, MLB still wanted human umpires to shout out decisions as they had in their 33__________.', 'F', 33),
(@group7_id, 6, 'The umpire''s job had, at one time, required a 34__________ about whether a ball was a strike.', 'D', 34),
(@group7_id, 6, 'A ball is considered a strike when the batter does not hit it and it crosses through a 35__________ extending approximately from the batter''s knee to his chest.', 'H', 35),
(@group7_id, 6, 'In the past, 36__________ over strike calls were not uncommon, but today everyone accepts the complete ban on pushing or shoving the umpire.', 'B', 36),
(@group7_id, 6, 'One difference, however, is that during the first game DeJesus used ABS, strike calls were met with 37__________.', 'G', 37);

-- D. Group 8 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 38-40: Choose the correct letter, A, B, C or D.', 3);
SET @group8_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group8_id, 3, 'What does the writer suggest about ABS in the fifth paragraph?', '{"A": "It is bound to make key decisions that are wrong.", "B": "It may reduce some of the appeal of the game.", "C": "It will lead to the disappearance of human umpires.", "D": "It may increase calls for the rules of baseball to be changed."}', 'B', 38),
(@group8_id, 3, 'Morgan Sword says that the introduction of ABS', '{"A": "was regarded as an experiment without a guaranteed outcome.", "B": "was intended to keep up with developments in other sports.", "C": "was a response to changing attitudes about the role of sport.", "D": "was an attempt to ensure baseball retained a young audience."}', 'D', 39),
(@group8_id, 3, 'Why does the writer include the views of Noë and Russo?', '{"A": "to show that attitudes to technology vary widely", "B": "to argue that people have unrealistic expectations of sport", "C": "to indicate that accuracy is not the same thing as enjoyment", "D": "to suggest that the number of baseball fans needs to increase"}', 'C', 40);

-- -----------------------------------------------------------------
-- 📝 FULL TEST IMPORT: Frozen Food / Coral Reefs / Robots
-- -----------------------------------------------------------------

USE ielts_db;

-- 1. Create the Test Record
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 4 (Frozen Food/Coral/Robots)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- =================================================================
-- ❄️ PASSAGE 1: FROZEN FOOD
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Frozen Food',
    '<p>A US perspective on the development of the frozen food industry</p><p>At some point in history, humans discovered that ice preserved food. There is evidence that winter ice was stored to preserve food in the summer as far back as 10,000 years ago. Two thousand years ago, the inhabitants of South America''s Andean mountains had a unique means of conserving potatoes for later consumption. They froze them overnight, then trampled them to squeeze out the moisture, then dried them in the sun. This preserved their nutritional value-if not their aesthetic appeal.</p><p>Natural ice remained the main form of refrigeration until late in the 19th century. In the early 1800s, ship owners from Boston, USA, had enormous blocks of Arctic ice towed all over the Atlantic for the purpose of food preservation. In 1851, railroads first began putting blocks of ice in insulated rail cars to send butter from Ogdensburg, New York, to Boston.</p><p>Finally, in 1870, Australian inventors found a way to make ''mechanical ice''. They used a compressor to force a gas-ammonia at first and later Freon-through a condenser. The compressed gas gave up some of its heat as it moved through the condenser. Then the gas was released quickly into a low-pressure evaporator coil where it became liquid and cold. Air was blown over the evaporator coil and then this cooled air passed into an insulated compartment, lowering its temperature to freezing point.</p><p>Initially, this process was invented to keep Australian beer cool even in hot weather. But Australian cattlemen were quick to realize that, if they could put this new invention on a ship, they could export meat across the oceans. In 1880, a shipment of Australian beef and mutton was sent, frozen, to England. While the food frozen this way was still palatable, there was some deterioration. During the freezing process, crystals formed within the cells of the food, and when the ice expanded and the cells burst, this spoilt the flavor and texture of the food.</p><p>The modern frozen food industry began with the indigenous Inuit people of Canada. In 1912, a biology student in Massachusetts, USA, named Clarence Birdseye, ran out of money and went to Labrador in Canada to trap and trade furs. While he was there, he became fascinated with how the Inuit would quickly freeze fish in the Arctic air. The fish looked and tasted fresh even months later.</p><p>Birdseye returned to the USA in 1917 and began developing mechanical freezers capable of quick-freezing food. Birdseye methodically kept inventing better freezers and gradually built a business selling frozen fish from Gloucester, Massachusetts. In 1929, his business was sold and became General Foods, but he stayed with the company as director of research, and his division continued to innovate.</p><p>Birdseye was responsible for several key innovations that made the frozen food industry possible. He developed quick-freezing techniques that reduced the damage that crystals caused, as well as the technique of freezing the product in the package it was to be sold in. He also introduced the use of cellophane, the first transparent material for food packaging, which allowed consumers to see the quality of the product. Birdseye products also came in convenient size packages that could be prepared with a minimum of effort.</p><p>But there were still obstacles. In the 1930s, few grocery stores could afford to buy freezers for a market that wasn''t established yet. So, Birdseye leased inexpensive freezer cases to them. He also leased insulated railroad cars so that he could ship his products nationwide. However, few consumers had freezers large enough or efficient enough to take advantage of the products.</p><p>Sales increased in the early 1940s, when World War II gave a boost to the frozen food industry because tin was being used for munitions. Canned foods were rationed to save tin for the war effort, while frozen foods were abundant and cheap. Finally, by the 1950s, refrigerator technology had developed far enough to make these appliances affordable for the average family. By 1953, 33 million US families owned a refrigerator, and manufacturers were gradually increasing the size of the freezer compartments in them.</p><p>1950s families were also looking for convenience at mealtimes, so the moment was right for the arrival of the ''TV Dinner''. Swanson Foods was a large, nationally recognized producer of canned and frozen poultry. In 1954, the company adapted some of Birdseye''s freezing techniques, and with the help of a clever name and a huge advertising budget, it launched the first ''TV Dinner''. This consisted of frozen turkey, potatoes and vegetables served in the same segmented aluminum tray that was used by airlines. The product was an instant success. Within a year, Swanson had sold 13 million TV dinners. American consumers couldn''t resist the combination of a trusted brand name, a single-serving package and the convenience of a meal that could be ready after only 25 minutes in a hot oven. By 1959, Americans were spending $2.7 billion annually on frozen foods, and half a billion of that was spent on ready-prepared meals such as the TV Dinner.</p><p>Today, the US frozen food industry has a turnover of over $67 billion annually, with $26.6 billion of that sold to consumers for home consumption. The remaining $40 billion in frozen food sales come through restaurants, cafeterias, hospitals and schools, and that represents a third of the total food service sales.</p>',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'People conserved the nutritional value of 1______________, using a method of freezing then drying.', 'potatoes', 1),
(@group1_id, 8, '2______________ was kept cool by ice during transportation in specially adapted trains.', 'butter', 2),
(@group1_id, 8, 'Two kinds of 3______________ were the first frozen food shipped to England.', 'meat', 3),
(@group1_id, 8, 'quick-freezing methods, so that 4______________ did not spoil the food.', 'crystals', 4),
(@group1_id, 8, 'packaging products with 5______________ so the product was visible.', 'cellophane', 5),
(@group1_id, 8, 'Frozen food became popular because of a shortage of 6______________', 'tin', 6),
(@group1_id, 8, 'A large number of homes now had a 7______________', 'refrigerator', 7);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Type 1: True/False/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'The ice transportation business made some Boston ship owners very wealthy in the early 1800s.', 'NOT GIVEN', 8),
(@group2_id, 1, 'A disadvantage of the freezing process invented in Australia was that it affected the taste of food.', 'TRUE', 9),
(@group2_id, 1, 'Clarence Birdseye travelled to Labrador in order to learn how the Inuit people froze fish.', 'FALSE', 10),
(@group2_id, 1, 'Swanson Foods invested a great deal of money in the promotion of the TV Dinner.', 'TRUE', 11),
(@group2_id, 1, 'Swanson Foods developed a new style of container for the launch of the TV Dinner.', 'FALSE', 12),
(@group2_id, 1, 'The US frozen food industry is currently the largest in the world.', 'NOT GIVEN', 13);


-- =================================================================
-- 🐠 PASSAGE 2: CORAL REEFS
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Can the planet''s coral reefs be saved?',
    '<p>A. Conservationists have put the final touches to a giant artificial reef they have been assembling at the world-renowned Zoological Society of London (London Zoo). Samples of the planet''s most spectacular corals - vivid green branching coral, yellow scroll, blue ridge and many more species - have been added to the giant tank along with fish that thrive in their presence: blue tang, clownfish and many others. The reef is in the zoo''s new gallery, Tiny Giants, which is dedicated to the minuscule invertebrate creatures that sustain life across the planet. The coral reef tank and its seven-metre-wide window form the core of the exhibition. ''Coral reefs are the most diverse ecosystems on Earth and we want to show people how wonderful they are,'' said Paul Pearce-Kelly, senior curator of invertebrates and fish at the Zoological Society of London. ''However, we also want to highlight the research and conservation efforts that are now being carried out to try to save them from the threat of global warming.'' They want people to see what is being done to try to save these wonders.</p><p>B. Corals are composed of tiny animals, known as polyps, with tentacles for capturing small marine creatures in the sea water. These polyps are transparent but get their brilliant tones of pink, orange, blue, green, etc. from algae that live within them, which in turn get protection, while their photosynthesising of the sun''s rays provides nutrients for the polyps. This comfortable symbiotic relationship has led to the growth of coral reefs that cover 0.1% of the planet''s ocean bed while providing homes for more than 25% of marine species, including fish, molluscs, sponges and shellfish.</p><p>C. As a result, coral reefs are often described as the ''rainforests of the sea'', though the comparison is dismissed by some naturalists, including David Attenborough. ''People say you cannot beat the rainforest,'' Attenborough has stated. ''But that is simply not true. You go there and the first thing you think is: where are the birds? Where are the animals? They are hiding in the trees, of course. No, if you want beauty and wildlife, you want a coral reef. Put on a mask and stick your head under the water. The sight is mind-blowing.''</p><p>D. Unfortunately, these majestic sights are now under very serious threat, with the most immediate problem coming in the form of thermal stress. Rising ocean temperatures are triggering bleaching events that strip reefs of their colour and eventually kill them. And that is just the start. Other menaces include ocean acidification, sea level increase, pollution by humans, deoxygenation and ocean current changes, while the climate crisis is also increasing habitat destruction. As a result, vast areas - including massive chunks of Australia''s Great Barrier Reef - have already been destroyed, and scientists advise that more than 90% of reefs could be lost by 2050 unless urgent action is taken to tackle global heating and greenhouse gas emissions. Pearce-Kelly says that coral reefs have to survive really harsh conditions - wave erosion and other factors. And ''when things start to go wrong in the oceans, then corals will be the first to react. And that is exactly what we are seeing now. Coral reefs are dying and they are telling us that all is not well with our planet.''</p><p>E. However, scientists are trying to pinpoint hardy types of coral that could survive our overheated oceans, and some of this research will be carried out at London Zoo. ''Behind our ... coral reef tank we have built laboratories where scientists will be studying coral species,'' said Pearce-Kelly. One aim will be to carry out research on species to find those that can survive best in warm, acidic waters. Another will be to try to increase coral breeding rates. ''Coral spawn just once a year,'' he added. ''However, aquarium-based research has enabled some corals to spawn artificially, which can assist coral reef restoration efforts. And if this can be extended for all species, we could consider the launching of coral-spawning programmes several times a year. That would be a big help in restoring blighted reefs.''</p><p>F. Research in these fields is being conducted in laboratories around the world, with the London Zoo centre linked to this global network. Studies carried out in one centre can then be tested in others. The resulting young coral can then be displayed in the tank in Tiny Giants. ''The crucial point is that the progress we make in making coral better able to survive in a warming world can be shown to the public and encourage them to believe that we can do something to save the planet''s reefs,'' said Pearce-Kelly. ''Saving our coral reefs is now a critically important ecological goal.''</p>',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Headings)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-19: Reading Passage 2 has six sections, A-F. Choose the correct heading for each section from the list of headings below. i. Tried and tested solutions, ii. Cooperation beneath the waves, iii. Working to lessen the problems, iv. Disagreement about the accuracy of a certain phrase, v. Two clear educational goals, vi. Promoting hope, vii. A warning of further trouble ahead', 1);
SET @group3_id = LAST_INSERT_ID();
-- Type 4: Matching Headings
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 4, 'Section A', 'v', 14),
(@group3_id, 4, 'Section B', 'ii', 15),
(@group3_id, 4, 'Section C', 'iv', 16),
(@group3_id, 4, 'Section D', 'vii', 17),
(@group3_id, 4, 'Section E', 'iii', 18),
(@group3_id, 4, 'Section F', 'vi', 19);

-- C. Group 4 (Multiple Choice - Pick Two)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 20-21: Choose TWO letters, A-E.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group4_id, 3, 'Which TWO of these causes of damage to coral reefs are mentioned by the writer of the text?', '{"A": "a rising number of extreme storms", "B": "the removal of too many fish from the sea", "C": "the contamination of the sea from waste", "D": "increased disease among marine species", "E": "alterations in the usual flow of water in the seas"}', 'C, E', 20);
-- Note: Q20 and Q21 are grouped as one entry here for scoring purposes, or you can split them.

-- D. Group 5 (Multiple Choice - Pick Two)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 22-23: Choose TWO letters, A-E.', 3);
SET @group5_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Which TWO of the following statements are true of the researchers at London Zoo?', '{"A": "They are hoping to expand the numbers of different corals being bred in laboratories.", "B": "They want to identify corals that can cope well with the changed sea conditions.", "C": "They are looking at ways of creating artificial reefs that corals could grow on.", "D": "They are trying out methods that would speed up reproduction in some corals.", "E": "They are investigating materials that might protect reefs from higher temperatures."}', 'B, D', 22);

-- E. Group 6 (Sentence Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 24-26: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 4);
SET @group6_id = LAST_INSERT_ID();
-- Type 7: Sentence Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 7, 'Corals have a number of 24______________ which they use to collect their food.', 'tentacles', 24),
(@group6_id, 7, 'Algae gain 25______________from being inside the coral.', 'protection', 25),
(@group6_id, 7, 'Increases in the warmth of the sea water can remove the 26______________ from coral.', 'colour', 26);


-- =================================================================
-- 🤖 PASSAGE 3: ROBOTS AND US
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Robots and us',
    '<p>Three leaders in their fields answer questions about our relationships with robot</p><p>When asked ''Should robots be used to colonise other planets?'', cosmology and astrophysics Professor Martin Rees said he believed the solar system would be mapped by robotic craft by the end of the century. ''The next step would be mining of asteroids, enabling fabrication of large structures in space without having to bring all the raw materials from Earth.... I think this is more realistic and benign than the … "terraforming"* of planets.'' He maintains that colonised planets ''should be preserved with a status that is analogous to Antarctica here on Earth.''</p><p>On the question of using robots to colonise other planets and exploit mineral resources, engineering Professor Daniel Wolpert replied, ''I don''t see a pressing need to colonise other planets unless we can bring [these] resources back to Earth. The vast majority of Earth is currently inaccessible to us. Using robots to gather resources nearer to home would seem to be a better use of our robotic tools.''</p><p>Meanwhile, for anthropology Professor Kathleen Richardson, the idea of ''colonisation'' of other planets seemed morally dubious: ''I think whether we do something on Earth or on Mars we should always do it in the spirit of a genuine interest in "the Other", not to impose a particular model, but to meet "the Other".''</p><p>In response to the second question, ''How soon will machine intelligence outstrip human intelligence?'', Rees mentions robots that are advanced enough to beat humans at chess, but then goes on to say, ''Robots are still limited in their ability to sense their environment: they can''t yet recognise and move the pieces on a real chessboard as cleverly as a child can. Later this century, however, their more advanced successors may relate to their surroundings, and to people, as adeptly as we do. Moral questions then arise. ... Should we feel guilty about exploiting [sophisticated robots]? Should we fret if they are underemployed, frustrated, or bored?''</p><p>Wolpert''s response to the question about machine intelligence outstripping human intelligence was this: ''In a limited sense it already has. Machines can already navigate, remember and search for items with an ability that far outstrips humans. However, there is no machine that can identify visual objects or speech with the reliability and flexibility of humans.... Expecting a machine close to the creative intelligence of a human within the next 50 years would be highly ambitious.''</p><p>Richardson believes that our fear of machines becoming too advanced has more to do with human nature than anything intrinsic to the machines themselves. In her view, it stems from humans'' tendency to personify inanimate objects: we create machines based on representations of ourselves, imagine that machines think and behave as we do, and therefore see them as an autonomous threat. ''One of the consequences of thinking that the problem lies with machines is that we tend to imagine they are greater and more powerful than they really are and subsequently they become so.''</p><p>This led on to the third question, ''Should we be scared by advances in artificial intelligence?'' To this question, Rees replied, ''Those who should be worried are the futurologists who believe in the so-called "singularity".** ... And another worry is that we are increasingly dependent on computer networks, and that these could behave like a single "brain" with a mind of its own, and with goals that may be contrary to human welfare. I think we should ensure that robots remain as no more than "idiot savants" lacking the capacity to outwit us, even though they may greatly surpass us in the ability to calculate and process information.''</p><p>Wolpert''s response was to say that we have already seen the damaging effects of artificial intelligence in the form of computer viruses. ''But in this case,'' he says, ''the real intelligence is the malicious designer. Critically, the benefits of computers outweigh the damage that computer viruses cause. Similarly, while there may be misuses of robotics in the near future, the benefits that they will bring are likely to outweigh these negative aspects.''</p><p>Richardson''s response to this question was this: ''We need to ask why fears of artificial intelligence and robots persist; none have in fact risen up and challenged human supremacy.'' She believes that as robots have never shown themselves to be a threat to humans, it seems unlikely that they ever will. In fact, she went on, ''Not all fear [robots]; many people welcome machine intelligence.''</p><p>In answer to the fourth question, What can science fiction tell us about robotics?'', Rees replied, ''I sometimes advise students that it''s better to read first-rate science fiction than second-rate science more stimulating, and perhaps no more likely to be wrong.''</p><p>As his response, Wolpert commented, ''Science fiction has often been remarkable at predicting the future. Science fiction has painted a vivid spectrum of possible futures, from cute and helpful robots to dystopian robotic societies. Interestingly, almost no science fiction envisages a future without robots.''</p><p>Finally, on the question of science fiction, Richardson pointed out that in modern society, people tend to think there is reality on the one hand, and fiction and fantasy on the other. She then explained that the division did not always exist, and that scientists and technologists made this separation because they wanted to carve out the sphere of their work. ''But the divide is not so clear cut, and that is why the worlds seem to collide at times,'' she said. ''In some cases, we need to bring these different understandings together to get a whole perspective. Perhaps then, we won''t be so frightened that something we create as a copy of ourselves will be a [threat] to us.''</p><p>*terraforming: modifying a planet''s atmosphere to suit human needs<br>** singularity: the point when robots will be able to start creating ever more sophisticated versions of themselves</p>',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 7 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-33: Match each statement with the correct expert, A, B or C. A. Martin Rees, B. Daniel Wolpert, C. Kathleen Richardson', 1);
SET @group7_id = LAST_INSERT_ID();
-- Type 6: Matching Features
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
VALUES (@passage3_id, 'Questions 34-36: Complete each sentence with the correct ending, A-D, below. A. robots to explore outer space, B. advances made in machine intelligence so far, C. changes made to other planets for our own benefit, D. the harm already done by artificial intelligence.', 2);
SET @group8_id = LAST_INSERT_ID();
-- Type 6: Matching Features
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 6, 'Richardson and Rees express similar views regarding the ethical aspect of', 'C', 34),
(@group8_id, 6, 'Rees and Wolpert share an opinion about the extent of', 'B', 35),
(@group8_id, 6, 'Wolpert disagrees with Richardson on the question of', 'D', 36);

-- D. Group 9 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37-40: Choose the correct letter, A, B, C or D.', 3);
SET @group9_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group9_id, 3, 'What point does Richardson make about fear of machines?', '{"A": "lt has grown alongside the development of ever more advanced robots.", "B": "It is the result of our inclination to attribute human characteristics to non-human entities.", "C": "It has its origins in basic misunderstandings about how inanimate objects function.", "D": "It demonstrates a key difference between human intelligence and machine intelligence."}', 'B', 37),
(@group9_id, 3, 'What potential advance does Rees see as a cause for concern?', '{"A": "robots outnumbering people", "B": "robots having abilities which humans do not", "C": "artificial intelligence developing independent thought", "D": "artificial intelligence taking over every aspect of our lives"}', 'C', 38),
(@group9_id, 3, 'What does Wolpert emphasise in his response to the question about science fiction?', '{"A": "how science fiction influences our attitudes to robots", "B": "how fundamental robots are to the science fiction genre", "C": "how the image of robots in science fiction has changed over time", "D": "how reactions to similar portrayals of robots in science fiction may vary"}', 'B', 39),
(@group9_id, 3, 'What is Richardson doing in her comment about reality and fantasy?', '{"A": "warning people not to confuse one with the other", "B": "outlining ways in which one has impacted on the other", "C": "recommending a change of approach in how people view them", "D": "explaining why scientists have a different perspective on them from other people"}', 'C', 40);

-- -----------------------------------------------------------------
-- END OF FILE
-- -----------------------------------------------------------------

-- -----------------------------------------------------------------
-- 📝 FULL TEST IMPORT: O'Keeffe / Climate Change / Guard Dogs
-- -----------------------------------------------------------------

USE ielts_db;

-- 1. Create the Test Record
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 5 (O''Keeffe/Climate/Dogs)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- =================================================================
-- 🎨 PASSAGE 1: GEORGIA O'KEEFFE
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Georgia O''Keeffe',
    '<p>For seven decades, Georgia O''Keeffe (1887-1986) was a major figure in American art. Remarkably, she remained independent from shifting art trends and her work stayed true to her own vision, which was based on finding the essential, abstract forms in nature. With exceptionally keen powers of observation and great finesse with a paintbrush, she recorded subtle nuances of colour, shape, and light that enlivened her paintings and attracted a wide audience.</p><p>Born in 1887 near Sun Prairie, Wisconsin to cattle breeders Francis and Ida O''Keeffe, Georgia was raised on their farm along with her six siblings. By the time she graduated from high school in 1905, she had determined to make her way as an artist. She studied the techniques of traditional painting at the Art Institute of Chicago school (1905) and the Art Students League of New York (1907-8). After attending university and then training college, she became an art teacher and taught in elementary schools, high schools, and colleges in Virginia, Texas, and South Carolina from 1911 to 1918.</p><p>During this period, O''Keeffe began to experiment with creating abstract compositions in charcoal, and produced a series of innovative drawings that led her art in a new direction. She sent some of these drawings to a friend in New York, who showed them to art collector and photographer Alfred Stieglitz in January 1916. Stieglitz was impressed, and exhibited the drawings later that year at his gallery on Fifth Avenue, New York City, where the works of many avant-garde artists and photographers were introduced to the American public.</p><p>With Stieglitz''s encouragement and promise of financial support, O''Keeffe arrived in New York in June 1918 to begin a career as an artist. For the next three decades, Stieglitz vigorously promoted her work in twenty-two solo exhibitions and numerous group installations. The two were married in 1924. The ups and downs of their personal and professional relationship were recorded in Stieglitz''s celebrated black-and-white portraits of O''Keeffe, taken over the course of twenty years (1917-37).</p><p>By the mid-1920s, O''Keeffe was recognized as one of America''s most important and successful artists, widely known for the architectural pictures that dramatically depict the soaring skyscrapers of New York. But most often, she painted botanical subjects, inspired by annual trips to the Stieglitz family summer home. In her magnified images depicting flowers, begun in 1924, O''Keeffe brings the viewer right into the picture.</p><p>Enlarging the tiniest details to fill an entire metre-wide canvas emphasized their shapes and lines and made them appear abstract. Such daring compositions helped establish O''Keeffe''s reputation as an innovative modernist.</p><p>In 1929, O''Keeffe made her first extended trip to the state of New Mexico. It was a visit that had a lasting impact on her life, and an immediate effect on her work. Over the next two decades she made almost annual trips to New Mexico, staying up to six months there, painting in relative solitude, then returning to New York each winter to exhibit the new work at Stieglitz''s gallery. This pattern continued until she moved permanently to New Mexico in 1949.</p><p>There, O''Keeffe found new inspiration: at first, it was the numerous sun-bleached bones she came across in the state''s rugged terrain that sparked her imagination. Two of her earliest and most celebrated Southwestern paintings exquisitely reproduce a cow skull''s weathered surfaces, jagged edges, and irregular openings. Later, she also explored another variation on this theme in her large series of Pelvis pictures, which focused on the contrasts between convex and concave surfaces, and solid and open spaces.</p><p>However, it was the region''s spectacular landscape, with its unusual geological formations, vivid colours, clarity of light, and exotic vegetation, that held the artist''s imagination for more than four decades. Often, she painted the rocks, cliffs, and mountains in striking close-up, just as she had done with her botanical subjects.</p><p>O''Keeffe eventually owned two homes in New Mexico - the first, her summer retreat at Ghost Ranch, was nestled beneath 200-metre cliffs, while the second, used as her winter residence, was in the small town of Abiquiú. While both locales provided a wealth of imagery for her paintings, one feature of the Abiquiú house - the large walled patio with its black door - was particularly inspirational. In more than thirty pictures between 1946 and 1960, she reinvented the patio into an abstract arrangement of geometric shapes.</p><p>From the 1950s into the 1970s, O''Keeffe travelled widely, making trips to Asia, the Middle East, and Europe. Flying in planes inspired her last two major series - aerial views of rivers and expansive paintings of the sky viewed from just above clouds. In both series, O''Keeffe increased the size of her canvases, sometimes to mural proportions, reflecting perhaps her newly expanded view of the world. When in 1965 she successfully translated one of her cloud motifs to a monumental canvas measuring 6 metres in length (with the help of assistants), it was an enormous challenge and a special feat for an artist nearing eighty years of age.</p><p>The last two decades of the artist''s life were relatively unproductive as ill health and blindness hindered her ability to work. O''Keeffe died in 1986 at the age of ninety-eight, but her rich legacy of some 900 paintings has continued to attract subsequent generations of artists and art lovers who derive inspiration from these very American images.</p>',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 1);
SET @group1_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 8, 'studied art, then worked as a 1______________ in various places in the USA', 'teacher', 1),
(@group1_id, 8, 'created drawings using 2______________ which were exhibited in New York City', 'charcoal', 2),
(@group1_id, 8, 'moved to New York and became famous for her paintings of the city''s 3______________', 'skyscrapers', 3),
(@group1_id, 8, 'produced a series of innovative close-up paintings of 4______________', 'flowers', 4),
(@group1_id, 8, 'went to New Mexico and was initially inspired to paint the many 5______________ that could be found there', 'bones', 5),
(@group1_id, 8, 'continued to paint various features that together formed the dramatic 6______________ of New Mexico for over forty years', 'landscape', 6),
(@group1_id, 8, 'travelled widely by plane in later years, and painted pictures of clouds and 7______________seen from above', 'rivers', 7);

-- C. Group 2 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 2);
SET @group2_id = LAST_INSERT_ID();
-- Type 1: True/False/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 1, 'Georgia O''Keeffe''s style was greatly influenced by the changing fashions in art over the seven decades of her career.', 'FALSE', 8),
(@group2_id, 1, 'When O''Keeffe finished high school, she had already made her mind up about the career that she wanted.', 'TRUE', 9),
(@group2_id, 1, 'Alfred Stieglitz first discovered O''Keeffe''s work when she sent some abstract drawings to his gallery in New York City.', 'FALSE', 10),
(@group2_id, 1, 'O''Keeffe was the subject of Stieglitz''s photographic work for many years.', 'TRUE', 11),
(@group2_id, 1, 'O''Keeffe''s paintings of the patio of her house in Abiquiú were among the artist''s favourite works.', 'NOT GIVEN', 12),
(@group2_id, 1, 'O''Keeffe produced a greater quantity of work during the 1950s to 1970s than at any other time in her life.', 'NOT GIVEN', 13);


-- =================================================================
-- 🌍 PASSAGE 2: ADAPTING TO CLIMATE CHANGE
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Adapting to the effects of climate change',
    '<p>A. All around the world, nations are already preparing for, and adapting to, climate change and its impacts. Even if we stopped all CO2 emissions tomorrow, we would continue to see the impact of the CO2 already released since industrial times, with scientists forecasting that global warming would continue for around 40 years. In the meantime, ice caps would continue to melt and sea levels rise. Some countries and regions will suffer more extreme impacts from these changes than others. It''s in these places that innovation is thriving.</p><p>B. In Miami Beach, Florida, USA, seawater isn''t just breaching the island city''s walls, it''s seeping up through the ground, so the only way to save the city is to lift it up above sea level. Starting in the lowest and most vulnerable neighbourhoods, roads have been raised by as much as 61 centimetres. The elevation work was carried out as part of Miami Beach''s ambitious but much-needed stormwater-management programme. In addition to the road adaptations, the city has set up new pumps that can remove up to 75,000 litres of water per minute. In the face of floods, climate-mitigation strategies have often been overlooked, says Yanira Pineda, a senior sustainability coordinator. She knows that they''re essential and that the job is far from over. ''We know that in 20, 30, 40 years, we''ll need to go back in there and adjust to the changing environment,'' she says.</p><p>C. Seawalls are a staple strategy for many coastal communities, but on the soft, muddy northern shores of Java, Indonesia, they frequently collapse, further exacerbating coastal erosion. There have been many attempts to restore the island''s coastal mangroves: ecosystems of trees and shrubs that help defend coastal areas by trapping sediment in their net-like root systems

[Image of Mangrove roots]
, elevating the sea bed and dampening the energy of waves and tidal currents. But Susanna Tol of the not-for-profit organisation Wetlands International says that, while hugely popular, the majority of mangrove-planting projects fail. So, Wetlands International started out with a different approach, building semi-permeable dams, made from bamboo poles and brushwood, to mimic the role of mangrove roots and create favourable conditions for mangroves to grow back naturally. The programme has seen moderate success, mainly in areas with less subsidence. "Unfortunately, traditional infrastructure is often single-solution focused,'' says Tol. ''For long-term success, it''s critical that we transition towards multifunctional approaches that embed natural processes and that engage and benefit communities and local decision-makers."</p><p>D. As the floodwaters rose in the rice fields of the Mekong Delta in September 2018, four small houses rose with them. Homes in this part of Vietnam are traditionally built on stilts but these ones had been built to float. The modifications were made by the Buoyant Foundation Project, a not-for-profit organisation that has been researching and retrofitting amphibious houses since 2006. ''When I started this,'' explains founder Elizabeth English, ''climate change was not on the tip of everybody''s tongue, but this technology is becoming necessary in places that didn''t previously need it. It''s much cheaper than permanently elevating houses, English explains - about a third of what it would cost to completely replace a building''s foundations. It also avoids the problem of taller houses being at greater risk from wind damage. Another plus comes from the fact that amphibious structures can be sensitively adapted to meet cultural needs and match the kind of houses that are already common in a community.</p><p>E. Bangladesh is especially vulnerable to climate change. Most of the country is less than a metre above sea level and 80 per cent of its land lies on floodplains. ''Almost 35 million people living on the coastal belt of Bangladesh are currently affected by soil and water salinity,'' says Raisa Chowdhury of the international development organisation ICCO Cooperation. Rather than fighting against it, one project is helping communities adapt to salt-affected soils. ICCO Cooperation has been working with 10,000 farmers in Bangladesh to start cultivating naturally salt-tolerant crops in the region. Certain varieties of carrot, potato, kohlrabi, cabbage and beetroot have been found to be better suited to salty soil than the rice and wheat that is typically grown there. Chowdhury says that the results are very visible, comparing a barren plot of land to the ''beautiful, lush green vegetable garden'' sitting beside it, in which he and his team have been working with the farmers. Since the project began, farmers trained in saline agriculture have reported increases of two to three more harvests per year.</p><p>F. Greg Spotts from Los Angeles (LA) in the USA is chief sustainability officer of the city''s street services department. He leads the Cool Streets LA programme, a series of pilot projects, which include the planting of trees and the installation of a ''cool pavement'' system, designed to help reach the city''s goal of bringing down its average temperature by 1.5°C. ''Urban cooling is literally a matter of life and death for our future in LA,'' says Spotts. Using a Geographic Information System data mapping tool, the programme identified streets with low tree canopy cover in three of the city''s neighbourhoods and covered them with a light-grey, light-reflecting coating, which had already been shown to lower road surface temperature in Los Angeles by 6°C. Spotts says one of these streets, in the Winnetka neighbourhood of San Fernando Valley, can now be seen as a pale crescent, the only cool spot on an otherwise red thermal image, from the International Space Station.</p>',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-17: Reading Passage 2 has six paragraphs, A-F. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Type 5: Matching Information
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'how a type of plant functions as a natural protection for coastlines', 'C', 14),
(@group3_id, 5, 'a prediction about how long it could take to stop noticing the effects of climate change', 'A', 15),
(@group3_id, 5, 'a reference to the fact that a solution is particularly cost-effective', 'D', 16),
(@group3_id, 5, 'a mention of a technology used to locate areas most in need of intervention', 'F', 17);

-- C. Group 4 (Sentence Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 18-22: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Type 7: Sentence Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 7, 'The stormwater-management programme in Miami Beach has involved the installation of efficient 18.______________.', 'pumps', 18),
(@group4_id, 7, 'The construction of 19______________ was the first stage of a project to ensure the success of mangroves in Indonesia.', 'dams', 19),
(@group4_id, 7, 'As a response to rising floodwaters in the Mekong Delta, a not-for-profit organisation has been building houses that can 20______________', 'float', 20),
(@group4_id, 7, 'Rising sea levels in Bangladesh have made it necessary to introduce various 21______________ that are suitable for areas of high salt content.', 'crops', 21),
(@group4_id, 7, 'A project in LA has increased the number of 22______________ on the city''s streets.', 'trees', 22);

-- D. Group 5 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23-26: Look at the following statements (23-26) and the list of people below. Match each statement with the correct person, A-E.\nList of People: A. Yanira Pineda, B. Susanna Tol, C. Elizabeth English, D. Raisa Chowdhury, E. Greg Spotts', 3);
SET @group5_id = LAST_INSERT_ID();
-- Type 6: Matching Features
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group5_id, 6, 'It is essential to adopt strategies which involve and help residents of the region.', 'B', 23),
(@group5_id, 6, 'Interventions which reduce heat are absolutely vital for our survival in this location.', 'E', 24),
(@group5_id, 6, 'More work will need to be done in future decades to deal with the impact of rising water levels.', 'A', 25),
(@group5_id, 6, 'The number of locations requiring action to adapt to flooding has grown in recent years.', 'C', 26);


-- =================================================================
-- 🐕 PASSAGE 3: LIVESTOCK GUARD DOGS
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'A new role for livestock guard dogs',
    '<p>Livestock guard dogs, traditionally used to protect farm animals from predators, are now being used to protect the predators themselves</p><p>A. For thousands of years, livestock guard dogs worked alongside shepherds to protect their sheep, goats and cattle from predators such as wolves and bears. But in the 19th and 20th centuries, when such predators were largely exterminated, most guard dogs lost their jobs. In recent years, however, as increased efforts have been made to protect wild animals, predators have become more widespread again. As a result, farmers once more need to protect their livestock, and guard dogs are enjoying an unexpected revival.</p><p>B. Today there are around 50 breeds of guard dogs on duty in various parts of the world. These dogs are raised from an early age with the animals they will be watching and eventually these animals become the dog''s family. The dogs will place themselves between the livestock and any threat, barking loudly. If necessary, they will chase away predators, but often their mere presence is sufficient. ''Their initial training is to make them understand that livestock is going to be their life,'' says Dan Macon, a shepherd with three guard dogs. ''A fluffy white puppy is fun to be around, but too much human affection makes it a great dog for guarding the front porch, rather than a great livestock guard dog.''</p><p>C. The evidence indicates that guard dogs are highly effective. For example, in Portugal, biologist Silvia Ribeiro has found that more than 90 per cent of the farmers participating in a programme to train and use guard dogs to protect their herds against attack from wolves rate the performance of the dogs as very good or excellent. In a study carried out in Australia by Linda van Bommel and Chris Johnson at the University of Tasmania, more than 65 per cent of herders reported that predation stopped completely after they got the dogs, and almost all the rest saw a decrease in attacks. ''If they are managed and used properly, livestock guard dogs are the most efficient control method that we have in terms of the amount of livestock that they save from predation,'' says van Bommel.</p><p>D. But today''s guard dogs also have a new role - to help preserve the predators. It is hoped that reductions in livestock losses can make farmers more tolerant of predators and less likely to kill them. In Namibia, more than 90 per cent of cheetahs live outside protected areas, close to humans raising livestock. As a result, the cheetahs are often held responsible for animal losses, and large numbers have been killed by farmers. When guard dogs were introduced, more than 90 per cent of farmers reported a dramatic reduction in livestock losses, and said that as a result they were less likely to kill predators. Julie Young, at Utah State University in the US, believes this result applies widely. "There is common ground from the livestock perspective and from the conservation perspective,'' she says. ''If ranchers don''t have a dead cow, they will not make a call to apply for a permit to kill a wolf."</p><p>E. Looking at all the published evidence, Bethany Smith at Nottingham Trent University in the UK found that up to 88 per cent of farmers said they no longer killed predators after using dogs - but warned that such self-reported results must be taken with a pinch of salt. What''s more, it is possible that livestock guard dogs merely displace predators to unprotected neighbouring properties, where their fate isn''t recorded. ''In some regions, we work with almost every farmer, but in others only one or two have dogs,'' says Ribeiro. ''If we are not working with everybody, we are transferring the wolf pressure to the neighbour''s herd and he can use poison and kill an entire pack of wolves.''</p><p>F. Another concern is whether there may be unintended ecological effects of using guard dogs. Studies suggest that reducing deaths of one type of predator may have a negative impact on other species. The extent of this problem isn''t known, but the consequences are clear in Namibia. Cheetahs aren''t the only species that cause sheep and goat losses there: other predators also attack livestock. In 2015, researchers reported that in spite of the impact farmers obtaining guard dogs had on cheetahs, the number of jackals killed by dogs and people actually increased. Guard dogs have other ecological impacts too. They have been found to spread diseases to wild animals, including endangered Ethiopian wolves. They may also compete with other carnivores for food. And by creating a ''landscape of fear'', their mere presence can influence the behaviour of prey animals.</p><p>G. The evidence so far, however, indicates that these consequences aren''t always negative. Guard dogs can deliver unexpected benefits by protecting vulnerable wildlife from predators. For example, their presence has been found to protect birds which build their nests on the ground in fields, where foxes would normally raid them. Indeed, Australian researchers are now using dogs to enhance biodiversity and create refuges for species threatened by predation. So if we can get this right, there may be a bright future for guard dogs in promoting harmonious coexistence between humans and wildlife.</p>',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 6 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-31: Reading Passage 3 has seven paragraphs, A-G. Which paragraph contains the following information?', 1);
SET @group6_id = LAST_INSERT_ID();
-- Type 5: Matching Information
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 5, 'an example of how one predator has been protected by the introduction of livestock guard dogs', 'D', 27),
(@group6_id, 5, 'an optimistic suggestion about the possible positive developments in the use of livestock guard dogs', 'G', 28),
(@group6_id, 5, 'a description of how the methods used by livestock guard dogs help to keep predators away', 'B', 29),
(@group6_id, 5, 'claims by different academics that the use of livestock guard dogs is a successful way of protecting farmers'' herds', 'C', 30),
(@group6_id, 5, 'a reference to how livestock guard dogs gain their skills', 'B', 31);

-- C. Group 7 (Matching Features - People)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 32-36: Look at the following statements (32-36) and the list of people below. Match each statement with the correct person, A-E.\nList of people: A. Dan Macon, B. Silvia Ribeiro, C. Linda van Bommel, D. Julie Young, E. Bethany Smith', 2);
SET @group7_id = LAST_INSERT_ID();
-- Type 6: Matching Features
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group7_id, 6, 'The use of guard dogs may save the lives of both livestock and wild animals.', 'D', 32),
(@group7_id, 6, 'Claims of a change in behaviour from those using livestock guard dogs may not be totally accurate.', 'E', 33),
(@group7_id, 6, 'There may be negative results if the use of livestock guard dogs is not sufficiently widespread.', 'B', 34),
(@group7_id, 6, 'Livestock guard dogs are the best way of protecting farm animals, as long as the dogs are appropriately handled.', 'C', 35),
(@group7_id, 6, 'Teaching a livestock guard dog how to do its work needs a different focus from teaching a house guard dog.', 'A', 36);

-- D. Group 8 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37-40: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 3);
SET @group8_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 8, 'This has led to a rise in the deaths of other predators, particularly 37______________. ', 'jackals', 37),
(@group8_id, 8, 'In addition, it has been suggested that the dogs could have 38______________ which may affect other species', 'diseases', 38),
(@group8_id, 8, 'and that they may reduce the amount of 39______________ available to certain wild animals.', 'food', 39),
(@group8_id, 8, 'These might otherwise be threatened by predators such as 40______________.', 'foxes', 40);

-- -----------------------------------------------------------------
-- 📝 FULL TEST IMPORT: Tennis Rackets / Pirates / Misinformation
-- -----------------------------------------------------------------

USE ielts_db;

-- 1. Create the Test Record
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 6 (Tennis/Pirates/Misinformation)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- =================================================================
-- 🎾 PASSAGE 1: HOW TENNIS RACKETS HAVE CHANGED
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'How tennis rackets have changed',
    '<p>In 2016, the British professional tennis player Andy Murray was ranked as the world''s number one. It was an incredible achievement by any standard – made even more remarkable by the fact that he did this during a period considered to be one of the strongest in the sport''s history, competing against the likes of Rafael Nadal, Roger Federer and Novak Djokovic, to name just a few. Yet five years previously, he had been regarded as a talented outsider who entered but never won the major tournaments.</p><p>Of the changes that account for this transformation, one was visible and widely publicised: in 2011, Murray invited former number one player Ivan Lendl onto his coaching team – a valuable addition that had a visible impact on the player''s playing style. Another change was so subtle as to pass more or less unnoticed. Like many players, Murray has long preferred a racket that consists of two types of string: one for the mains (verticals) and another for the crosses (horizontals). While he continued to use natural string in the crosses, in 2012 he switched to a synthetic string for the mains. A small change, perhaps, but its importance should not be underestimated.</p><p>The modification that Murray made is just one of a number of options available to players looking to tweak their rackets in order to improve their games. ''Touring professionals have their rackets customised to their specific needs,'' says Colin Triplow, a UK-based professional racket stringer. ''It''s a highly important part of performance maximisation.'' Consequently, the specific rackets used by the world''s elite are not actually readily available to the public; rather, each racket is individually made to suit the player who uses it. Take the US professional tennis players Mike and Bob Bryan, for example: ''We''re very particular with our racket specifications,'' they say. ''All our rackets are sent from our manufacturer to Tampa, Florida, where our frames go through a . . . thorough customisation process.'' They explain how they have adjusted not only racket length, but even experimented with different kinds of paint. The rackets they use now weigh more than the average model and also have a denser string pattern (i.e. more crosses and mains).</p><p>The primary reason for these modifications is simple: as the line between winning and losing becomes thinner and thinner, even these slight changes become more and more important. As a result, players and their teams are becoming increasingly creative with the modifications to their rackets as they look to maximise their competitive advantage.</p><p>Racket modifications mainly date back to the 1970s, when the amateur German tennis player Werner Fischer started playing with the so-called spaghetti-strung racket. It created a string bed that generated so much topspin that it was quickly banned by the International Tennis Federation. However, within a decade or two, racket modification became a regularity. Today it is, in many ways, an aspect of the game that is equal in significance to nutrition or training.</p><p>Modifications can be divided into two categories: those to the string bed and those to the racket frame. The former is far more common than the latter: the choice of the strings and the tension with which they are installed is something that nearly all professional players experiment with. They will continually change it depending on various factors including the court surface, climatic conditions, and game styles. Some will even change it depending on how they feel at the time.</p><p>At one time, all tennis rackets were strung with natural gut made from the outer layer of sheep or cow intestines. This all changed in the early 1990s with the development of synthetic strings that were cheaper and more durable. They are made from three materials: nylon (relatively durable and affordable), Kevlar (too stiff to be used alone) or co-polyester (polyester combined with additives that enhance its performance). Even so, many professional players continue to use a ''hybrid set-up'', where a combination of both synthetic and natural strings are used.</p><p>Of the synthetics, co-polyester is by far the most widely used. It''s a perfect fit for the style of tennis now played, where players tend to battle it out from the back of the court rather than coming to the net. Studies indicate that the average spin from a co-polyester string is 25% greater than that from natural string or other synthetics. In a sense, the development of co-polyester strings has revolutionised the game.</p><p>However, many players go beyond these basic adjustments to the strings and make changes to the racket frame itself. For example, much of the serving power of US professional player Pete Sampras was attributed to the addition of four to five lead weights onto his rackets, and today many professionals have the weight adjusted during the manufacturing process.</p><p>Other changes to the frame involve the handle. Players have individual preferences for the shape of the handle and some will have the handle of one racket moulded onto the frame of a different racket. Other players make different changes. The professional Portuguese player Gonçalo Oliveira replaced the original grips of his rackets with something thinner because they had previously felt uncomfortable to hold.</p><p>Racket customisation and modification have pushed the standards of the game to greater levels that few could have anticipated in the days of natural strings and heavy, wooden frames, and it''s exciting to see what further developments there will be in the future.</p>',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 1);
SET @group1_id = LAST_INSERT_ID();
-- Type 1: True/False/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 1, 'People had expected Andy Murray to become the world''s top tennis player for at least five years before 2016.', 'FALSE', 1),
(@group1_id, 1, 'The change that Andy Murray made to his rackets attracted a lot of attention.', 'FALSE', 2),
(@group1_id, 1, 'Most of the world''s top players take a professional racket stringer on tour with them.', 'NOT GIVEN', 3),
(@group1_id, 1, 'Mike and Bob Bryan use rackets that are light in comparison to the majority of rackets.', 'FALSE', 4),
(@group1_id, 1, 'Werner Fischer played with a spaghetti-strung racket that he designed himself.', 'NOT GIVEN', 5),
(@group1_id, 1, 'The weather can affect how professional players adjust the strings on their rackets.', 'TRUE', 6),
(@group1_id, 1, 'It was believed that the change Pete Sampras made to his rackets contributed to his strong serve.', 'TRUE', 7);

-- C. Group 2 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group2_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 8, 'Mike and Bob Bryan made changes to the types of 8………………… used on their racket frames.', 'paint', 8),
(@group2_id, 8, 'Players were not allowed to use the spaghetti-strung racket because of the amount of 9………………… it created.', 'topspin', 9),
(@group2_id, 8, 'Changes to rackets can be regarded as being as important as players'' diets or the 10………………… they do.', 'training', 10),
(@group2_id, 8, 'All rackets used to have natural strings made from the 11………………… of animals.', 'intestines', 11),
(@group2_id, 8, 'Pete Sampras had metal 12………………… put into the frames of his rackets.', 'weights', 12),
(@group2_id, 8, 'Gonçalo Oliveira changed the 13………………… on his racket handles.', 'grips', 13);


-- =================================================================
-- ⚓ PASSAGE 2: THE PIRATES OF THE ANCIENT MEDITERRANEAN
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The pirates of the ancient Mediterranean',
    '<p>A. When one mentions pirates, an image springs to most people''s minds of a crew of misfits, daredevils and adventurers in command of a tall sailing ship in the Caribbean Sea. Yet from the first to the third millennium BCE, thousands of years before these swashbucklers began spreading fear across the Caribbean, pirates prowled the Mediterranean, raiding merchant ships and threatening vital trade routes. However, despite all efforts and the might of various ancient states, piracy could not be stopped. The situation remained unchanged for thousands of years. Only when the pirates directly threatened the interests of ancient Rome did the Roman Republic organise a massive fleet to eliminate piracy. Under the command of the Roman general Pompey, Rome eradicated piracy, transforming the Mediterranean into ''Mare Nostrum'' (Our Sea).</p><p>B. Although piracy in the Mediterranean is first recorded in ancient Egypt during the reign of Pharaoh Amenhotep III (c 1390–1353 BCE), it is reasonable to assume it predated this powerful civilisation. This is partly due to the great importance the Mediterranean held at this time, and partly due to its geography. While the Mediterranean region is predominantly fertile, some parts are rugged and hilly, even mountainous. In the ancient times, the inhabitants of these areas relied heavily on marine resources, including fish and salt. Most had their own boats, possessed good seafaring skills, and unsurpassed knowledge of the local coastline and sailing routes. Thus, it is not surprising that during hardships, these men turned to piracy. Geography itself further benefited the pirates, with the numerous coves along the coast providing places for them to hide their boats and strike undetected. Before the invention of ocean-going caravels* in the 15th century, ships could not easily cross long distances over open water. Thus, in the ancient world most were restricted to a few well-known navigable routes that followed the coastline. Caught in a trap, a slow merchant ship laden with goods had no other option but to surrender. In addition, knowledge of the local area helped the pirates to avoid retaliation once a state fleet arrived.</p><p>C. One should also add that it was not unknown in the first and second millennia BCE for governments to resort to pirates'' services, especially during wartime, employing their skills and numbers against their opponents. A pirate fleet would serve in the first wave of attack, preparing the way for the navy. Some of the regions were known for providing safe harbours to pirates, who, in return, boosted the local economy.</p><p>D. The first known record of a named group of Mediterranean pirates, made during the rule of ancient Egyptian Pharaoh Akhenaten (c 1353–1336 BCE), was in the Amarna Letters. These were extracts of diplomatic correspondence between the pharaoh and his allies, and covered many pressing issues, including piracy. It seems the pharaoh was troubled by two distinct pirate groups, the Lukka and the Sherden. Despite the Egyptian fleet''s best efforts, the pirates continued to cause substantial disruption to regional commerce. In the letters, the king of Alashiya (modern Cyprus) rejected Akhenaten''s claims of a connection with the Lukka (based in modern-day Turkey). The king assured Akhenaten he was prepared to punish any of his subjects involved in piracy.</p><p>E. The ancient Greek world''s experience of piracy was different from that of Egyptian rulers. While Egypt''s power was land-based, the ancient Greeks relied on the Mediterranean in almost all aspects of life, from trade to warfare. Interestingly, in his works the Iliad and the Odyssey, the ancient Greek writer Homer not only condones, but praises the lifestyle and actions of pirates. The opinion remained unchanged in the following centuries. The ancient Greek historian Thucydides, for instance, glorified pirates'' daring attacks on ships or even cities. For Greeks, piracy was a part of everyday life. Even high-ranking members of the state were not beyond engaging in such activities. According to the Greek orator Demosthenes, in 355 BCE, Athenian ambassadors made a detour from their official travel to capture a ship sailing from Egypt, taking the wealth found onboard for themselves! The Greeks'' liberal approach towards piracy does not mean they always tolerated it, but attempts to curtail piracy were hampered by the large number of pirates operating in the Mediterranean.</p><p>F. The rising power of ancient Rome required the Roman Republic to deal with piracy in the Mediterranean. While piracy was a serious issue for the Republic, Rome profited greatly from its existence. Pirate raids provided a steady source of slaves, essential for Rome''s agriculture and mining industries. But this arrangement could work only while the pirates left Roman interests alone. Pirate attacks on grain ships, which were essential to Roman citizens, led to angry voices in the Senate, demanding punishment of the culprits. Rome, however, did nothing, further encouraging piracy. By the 1st century BCE, emboldened pirates kidnapped prominent Roman dignitaries, asking for a large ransom to be paid. Their most famous hostage was none other than Julius Caesar, captured in 75 BCE.</p><p>G. By now, Rome was well aware that pirates had outlived their usefulness. The time had come for concerted action. In 67 BCE, a new law granted Pompey vast funds to combat the Mediterranean menace. Taking personal command, Pompey divided the entire Mediterranean into 13 districts, assigning a fleet and commander to each. After cleansing one district of pirates, the fleet would join another in the next district. The process continued until the entire Mediterranean was free of pirates. Although thousands of pirates died at the hands of Pompey''s troops, as a long-term solution to the problem, many more were offered land in fertile areas located far from the sea. Instead of a maritime menace, Rome got productive farmers that further boosted its economy.</p>',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-19: Reading Passage 2 has seven paragraphs, A–G. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Type 5: Matching Information
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'a reference to a denial of involvement in piracy', 'D', 14),
(@group3_id, 5, 'details of how a campaign to eradicate piracy was carried out', 'G', 15),
(@group3_id, 5, 'a mention of the circumstances in which states in the ancient world would make use of pirates', 'C', 16),
(@group3_id, 5, 'a reference to how people today commonly view pirates', 'A', 17),
(@group3_id, 5, 'an explanation of how some people were encouraged not to return to piracy', 'G', 18),
(@group3_id, 5, 'a mention of the need for many sailing vessels to stay relatively close to land', 'B', 19);

-- C. Group 4 (Multiple Choice - Pick Two)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 20-21: Choose TWO letters, A-E. Which TWO of the following statements does the writer make about inhabitants of the Mediterranean region in the ancient world?', 2);
SET @group4_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group4_id, 3, 'Statements about inhabitants:', '{"A": "They often used stolen vessels to carry out pirate attacks.", "B": "They managed to escape capture by the authorities because they knew the area so well.", "C": "They paid for information about the routes merchant ships would take.", "D": "They depended more on the sea for their livelihood than on farming.", "E": "They stored many of the goods taken in pirate attacks in coves along the coastline."}', 'B, D', 20);

-- D. Group 5 (Multiple Choice - Pick Two)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 22-23: Choose TWO letters, A-E. Which TWO of the following statements does the writer make about piracy and ancient Greece?', 3);
SET @group5_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group5_id, 3, 'Statements about piracy and ancient Greece:', '{"A": "The state estimated that very few people were involved in piracy.", "B": "Attitudes towards piracy changed shortly after the Iliad and the Odyssey were written.", "C": "Important officials were known to occasionally take part in piracy.", "D": "Every citizen regarded pirate attacks on cities as unacceptable.", "E": "A favourable view of piracy is evident in certain ancient Greek texts."}', 'C, E', 22);

-- E. Group 6 (Summary Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 24-26: Complete the summary below. Choose ONE WORD ONLY from the passage for each answer.', 4);
SET @group6_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group6_id, 8, 'However, attacks on vessels transporting 24………………… to Rome resulted in calls', 'grain', 24),
(@group6_id, 8, 'calls for 25…………………for the pirates responsible.', 'punishment', 25),
(@group6_id, 8, 'some pirates demanding a 26………………… for the return of the Roman officials they captured.', 'ransom', 26);


-- =================================================================
-- 🧠 PASSAGE 3: THE PERSISTENCE AND PERIL OF MISINFORMATION
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The persistence and peril of misinformation',
    '<p>Brian Southwell looks at how human brains verify information and discusses some of the challenges of battling widespread falsehoods</p><p>Misinformation – both deliberately promoted and accidentally shared – is perhaps an inevitable part of the world in which we live, but it is not a new problem. People likely have lied to one another for roughly as long as verbal communication has existed. Deceiving others can offer an apparent opportunity to gain strategic advantage, to motivate others to action, or even to protect interpersonal bonds. Moreover, people inadvertently have been sharing inaccurate information with one another for thousands of years.</p><p>However, we currently live in an era in which technology enables information to reach large audiences distributed across the globe, and thus the potential for immediate and widespread effects from misinformation now looms larger than in the past. Yet the means to correct misinformation might, over time, be found in those same patterns of mass communication and of the facilitated spread of information.</p><p>The main worry regarding misinformation is its potential to unduly influence attitudes and behavior, leading people to think and act differently than they would if they were correctly informed, as suggested by the research teams of Stephan Lewandowsky of the University of Bristol and Elizabeth Marsh of Duke University, among others. In other words, we worry that misinformation might lead people to hold misperceptions (or false beliefs) and that these misperceptions, especially when they occur among large groups of people, may have detrimental, downstream consequences for health, social harmony, and the political climate.</p><p>At least three observations related to misinformation in the contemporary mass-media environment warrant the attention of researchers, policy makers, and really everyone who watches television, listens to the radio, or reads information online. First of all, people who encounter misinformation tend to believe it, at least initially. Secondly, electronic and print media often do not block many types of misinformation before it appears in content available to large audiences. Thirdly, countering misinformation once it has enjoyed wide exposure can be a resource-intensive effort.</p><p>Knowing what happens when people initially encounter misinformation holds tremendous importance for estimating the potential for subsequent problems. Although it is fairly routine for individuals to come across information that is false, the question of exactly how – and when – we mentally label information as true or false has garnered philosophical debate. The dilemma is neatly summarized by a contrast between how the 17th-century philosophers René Descartes and Baruch Spinoza described human information engagement, with conflicting predictions that only recently have been empirically tested in robust ways. Descartes argued that a person only accepts or rejects information after considering its truth or falsehood; Spinoza argued that people accept all encountered information (or misinformation) by default and then subsequently verify or reject it through a separate cognitive process. In recent decades, empirical evidence from the research teams of Erik Asp of the University of Chicago and Daniel Gilbert at Harvard University, among others, has supported Spinoza''s account: people appear to encode all new information as if it were true, even if only momentarily, and later tag the information as being either true or false, a pattern that seems consistent with the observation that mental resources for skepticism physically reside in a different part of the brain than the resources used in perceiving and encoding.</p><p>What about our second observation that misinformation often can appear in electronic or print media without being preemptively blocked? In support of this, one might consider the nature of regulatory structures in the United States: regulatory agencies here tend to focus on post hoc detection of broadcast information. Organizations such as the Food and Drug Administration (FDA) offer considerable monitoring and notification functions, but these roles typically do not involve preemptive censoring. The FDA oversees direct-to-consumer prescription drug advertising, for example, and has developed mechanisms such as the ''Bad Ad'' program, through which people can report advertising in apparent violation of FDA guidelines on drug risks. Such programs, although laudable and useful, do not keep false advertising off the airwaves. In addition, even misinformation that is successfully corrected can continue to affect attitudes.</p><p>This leads us to our third observation: a campaign to correct misinformation, even if rhetorically compelling, requires resources and planning to accomplish necessary reach and frequency. For corrective campaigns to be persuasive, audiences need to be able to comprehend them, which requires either effort to frame messages in ways that are accessible or effort to educate and sensitize audiences to the possibility of misinformation. That some audiences might be unaware of the potential for misinformation also suggests the utility of media literacy efforts as early as elementary school. Even with journalists and scholars pointing to the phenomenon of ''fake news'', people do not distinguish between demonstrably false stories and those based in fact when scanning and processing written information.</p><p>We live at a time when widespread misinformation is common. Yet at this time many people also are passionately developing potential solutions and remedies. The journey forward undoubtedly will be a long and arduous one. Future remedies will require not only continued theoretical consideration but also the development and maintenance of consistent monitoring tools – and a recognition among fellow members of society that claims which find prominence in the media that are insufficiently based in scientific consensus and social reality should be countered. Misinformation arises as a result of human fallibility and human information needs. To overcome the worst effects of the phenomenon, we will need coordinated efforts over time, rather than any singular one-time panacea we could hope to offer.</p>',
    3
);
SET @passage3_id = LAST_INSERT_ID();

-- B. Group 7 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 27-30: Choose the correct letter, A, B, C or D.', 1);
SET @group7_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group7_id, 3, 'What point does the writer make about misinformation in the first paragraph?', '{"A": "Misinformation is a relatively recent phenomenon.", "B": "Some people find it easy to identify misinformation.", "C": "Misinformation changes as it is passed from one person to another.", "D": "There may be a number of reasons for the spread of misinformation."}', 'D', 27),
(@group7_id, 3, 'What does the writer say about the role of technology?', '{"A": "It may at some point provide us with a solution to misinformation.", "B": "It could fundamentally alter the way in which people regard information.", "C": "It has changed the way in which organisations use misinformation.", "D": "It has made it easier for people to check whether information is accurate."}', 'A', 28),
(@group7_id, 3, 'What is the writer doing in the fourth paragraph?', '{"A": "comparing the different opinions people have of misinformation.", "B": "explaining how the effects of misinformation have changed over time", "C": "outlining which issues connected with misinformation are significant today", "D": "describing the attitude of policy makers towards misinformation in the media"}', 'C', 29),
(@group7_id, 3, 'What point does the writer make about regulation in the USA?', '{"A": "The guidelines issued by the FDA need to be simplified.", "B": "Regulation does not affect people''s opinions of new prescription drugs.", "C": "The USA has more regulatory bodies than most other countries.", "D": "Regulation fails to prevent misinformation from appearing in the media."}', 'D', 30);

-- C. Group 8 (Summary Completion with Box Options)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 31-36: Complete the summary using the list of phrases, A-J, below. A. constant conflict, B. additional evidence, C. different locations, D. experimental subjects, E. short period, F. extreme distrust, G. frequent exposure, H. mental operation, I. dubious reason, J. different ideas', 2);
SET @group8_id = LAST_INSERT_ID();
-- Type 8: Summary Completion
-- NOTE: For box options, the answer is the letter (G, J, H...)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 8, 'Although people have 31……………… to misinformation', 'G', 31),
(@group8_id, 8, 'The philosophers Descartes and Spinoza had 32……………… about how people engage with information.', 'J', 32),
(@group8_id, 8, 'Moreover, Spinoza believes that a distinct 33……………… is involved in these stages.', 'H', 33),
(@group8_id, 8, 'Recent research has provided 34……………… for Spinoza''s theory', 'B', 34),
(@group8_id, 8, 'even if this is for an extremely 35………………', 'E', 35),
(@group8_id, 8, 'This is consistent with the fact that the resources for scepticism and the resources for perceiving and encoding are in 36……………… in the brain.', 'C', 36);

-- D. Group 9 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage3_id, 'Questions 37-40: Do the following statements agree with the claims of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 3);
SET @group9_id = LAST_INSERT_ID();
-- Type 2: Yes/No/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group9_id, 2, 'Campaigns designed to correct misinformation will fail to achieve their purpose if people are unable to understand them.', 'YES', 37),
(@group9_id, 2, 'Attempts to teach elementary school students about misinformation have been opposed.', 'NOT GIVEN', 38),
(@group9_id, 2, 'It may be possible to overcome the problem of misinformation in a relatively short period.', 'NO', 39),
(@group9_id, 2, 'The need to keep up with new information is hugely exaggerated in today''s world.', 'NOT GIVEN', 40);

-- -----------------------------------------------------------------
-- 📝 FULL TEST IMPORT: Settlers / Wetlands / Speech Translation
-- -----------------------------------------------------------------

USE ielts_db;

-- 1. Create the Test Record
INSERT INTO reading_test (test_name, test_level)
VALUES ('Sample IELTS Reading Test 7 (Settlers/Wetlands/Translation)', 'Academic');
SET @test_id = LAST_INSERT_ID();

-- =================================================================
-- 🏝️ PASSAGE 1: ARCHAEOLOGISTS DISCOVER EVIDENCE OF PREHISTORIC ISLAND SETTLERS
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Archaeologists discover evidence of prehistoric island settlers',
    '<p>In early April 2019, Dr Ceri Shipton and his colleagues from Australian National University became the first archaeologists to explore Obi, one of many tropical islands in Indonesia’s Maluku Utara province. The research team’s discoveries suggest that the prehistoric people who lived on Obi were adept on both land and sea, hunting in the dense rainforest, foraging on the seashore, and possibly even voyaging between islands.</p><p>The excavations were part of a project to learn more about how people first dispersed from mainland Asia, through the Indonesian archipelago and into the prehistoric continent that once connected Australia and New Guinea. The team’s earlier research suggested that the northernmost islands in the group, known as the Wallacean islands, including Obi, would have offered the easiest migration route. It also seemed likely that these islands were crucial ‘stepping stones’ on humans’ island-hopping voyages through this region millennia ago. But to support this idea, they needed archaeological evidence for humans living in this remote area in the ancient past. So, they travelled to Obi to look for sites that might reveal evidence of early occupation.</p><p>Just inland from the village of Kelo on Obi’s northern coast, Shipton and his colleagues found two caves containing prehistoric rock shelters that were suitable for excavation. With the permission and help of the local people of Kelo, they dug a small test excavation in each shelter. There they found numerous artefacts, including fragments of axes, some dating to about 14,000 years ago. The earliest axes at Kelo were made using clam shells. Axes made from clam shells from roughly the same time had also previously been found elsewhere in this region, including on the nearby island of Gebe to the northeast. As on Gebe, it is highly likely that Obi’s axes were used in the construction of canoes, thus allowing these early peoples to maintain connections between communities on neighbouring islands.</p><p>The oldest cultural layers from the Kelo site provided the team with the earliest record for human occupation on Obi, dating back around 18,000 years. At this time the climate was drier and colder than today, and the island’s dense rainforests would likely have been much less impenetrable than they are now. Sea levels were about 120 metres lower, meaning Obi was a much larger island, encompassing what is today the separate island of Bisa, as well as several other small islands nearby.</p><p>Roughly 11,700 years ago, as the most recent ice age ended, the climate became significantly warmer and wetter, no doubt making Obi’s jungle much thicker. According to the researchers, it is no coincidence that around this time the first axes crafted from stone rather than sea shells appear, likely in response to their heavy-duty use for clearing and modification of the increasingly dense rainforest. While stone takes about twice as long to grind into an axe compared to shell, the harder material keeps its sharp edge for longer.</p><p>Judging by the bones which the researchers unearthed in the Kelo caves, people living there mainly hunted the Rothschild’s cuscus, a possum-like creature that still lives on Obi today. As the forest grew more dense, people probably used axes to clear patches of forest and make hunting easier.</p><p>Shipton’s team’s excavation of the shelters at the Kelo site unearthed a volcanic glass substance called obsidian, which must have been brought over from another island, as there is no known source on Obi. It also revealed particular types of beads, similar to those previously found on islands in southern Wallacea. These finds again support the idea that Obi islanders routinely travelled to other islands.</p><p>The excavations suggest people successfully lived in the two Kelo shelters for about 10,000 years. But then, about 8,000 years ago, both were abandoned. Did the residents leave Obi completely, or move elsewhere on the island? Perhaps the jungle had grown so thick that axes were no longer a match for the dense undergrowth. Perhaps people simply moved to the coast and turned to fishing rather than hunting as a means of survival.</p><p>Whatever the reason for the departure, there is no evidence for use of the Kelo shelters after this time, until about 1,000 years ago, when they were re-occupied by people who owned pottery as well as items made out of gold and silver. It seems likely, in view of Obi’s location, that this final phase of occupation also saw the Kelo shelters used by people involved in the historic trade in spices between the Maluku islands and the rest of the world.</p>',
    1
);
SET @passage1_id = LAST_INSERT_ID();

-- B. Group 1 (True/False/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 1-7: Do the following statements agree with the information given in Reading Passage 1? TRUE / FALSE / NOT GIVEN', 1);
SET @group1_id = LAST_INSERT_ID();
-- Type 1: True/False/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group1_id, 1, 'Archaeological research had taken place on the island of Obi before the arrival of Ceri Shipton and his colleagues.', 'FALSE', 1),
(@group1_id, 1, 'At the Kelo sites, the researchers found the first clam shell axes ever to be discovered in the region.', 'FALSE', 2),
(@group1_id, 1, 'The size of Obi today is less than it was 18,000 years ago.', 'TRUE', 3),
(@group1_id, 1, 'A change in the climate around 11,700 years ago had a greater impact on Obi than on the surrounding islands.', 'NOT GIVEN', 4),
(@group1_id, 1, 'The researchers believe there is a connection between warmer, wetter weather and a change in the material used to make axes.', 'TRUE', 5),
(@group1_id, 1, 'Shipton’s team were surprised to find evidence of the Obi islanders’ hunting practices.', 'NOT GIVEN', 6),
(@group1_id, 1, 'It is thought that the Kelo shelters were occupied continuously until about 1,000 years ago.', 'FALSE', 7);

-- C. Group 2 (Note Completion)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage1_id, 'Questions 8-13: Complete the notes below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group2_id = LAST_INSERT_ID();
-- Type 8: Summary Completion (Note Completion)
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group2_id, 8, 'Excavations of rock shelters inside 8………………… near the village of Kelo revealed:', 'caves', 8),
(@group2_id, 8, 'axes made out of 9…………………, dating from around 11,700 years ago', 'stone', 9),
(@group2_id, 8, '10………………… of an animal: evidence of what ancient islanders ate', 'bones', 10),
(@group2_id, 8, 'evidence of travel between islands: – obsidian: a material that is not found naturally on Obi – 11………………… which resembled ones found on other islands.', 'beads', 11),
(@group2_id, 8, 'Obi islanders had 12………………… as well as items made out of metal', 'pottery', 12),
(@group2_id, 8, 'probably took part in the production and sale of 13………………… .', 'spices', 13);


-- =================================================================
-- 💧 PASSAGE 2: THE GLOBAL IMPORTANCE OF WETLANDS
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'The global importance of wetlands',
    '<p>A. Wetlands are areas where water covers the soil, or is present either at or near the surface of the soil, for all or part of the year. These are complex ecosystems, rich in unique plant and animal life. But according to the World Wide Fund for Nature (WWFN), half of the world’s wetlands have disappeared since 1990 – converted or destroyed for commercial development, drainage schemes and the extraction of minerals and peat*. Many of those that remain have been damaged by agricultural pesticides and fertilizers, industrial pollutants, and construction works.</p><p>B. Throughout history, humans have gathered around wetlands, and their fertile ecosystems have played an important part in human development. Consequently, they are of considerable religious, historical and archaeological value to many communities around the world. ‘Wetlands directly support the livelihoods and well-being of millions of people,’ says Dr Matthew McCartney, principal researcher and hydrologist at the International Water Management Institute (IWMI). ‘In many developing countries, large numbers of people are dependent on wetland agriculture for their livelihoods.’</p><p>C. They also serve a crucial environmental purpose. ‘Wetlands are one of the key tools in mitigating climate change across the planet,’ says Pieter van Eijk, head of Climate Adaptation at Wetlands International (WI), pointing to their use as buffers that protect coastal areas from sea-level rise and extreme weather events such as hurricanes and flooding. Wetland coastal forests provide food and water, as well as shelter from storms, and WI and other agencies are working to restore those forests which have been lost. ‘It can be as simple as planting a few trees per hectare to create shade and substantially change a microclimate,’ he says. ‘Implementing climate change projects isn’t so much about money.’</p><p>D. The world’s wetlands are, unfortunately, rich sources for in-demand commodities, such as palm oil and pulpwood. Peatlands – wetlands with a waterlogged organic soil layer – are particularly targeted. When peatlands are drained for cultivation, they become net carbon emitters instead of active carbon stores, and, according to Marcel Silvius, head of Climate-smart Land-use at WI, this practice causes six per cent of all global carbon emissions. The clearance of peatlands for planting also increases the risk of forest fires, which release huge amounts of CO₂. ‘We’re seeing huge peatland forests with extremely high biodiversity value being lost for a few decades of oil palm revenues,’ says Silvius.</p><p>E. The damage starts when logging companies arrive to clear the trees. They dig ditches to enter the peat swamps by boat and then float the logs out the same way. These are then used to drain water out of the peatlands to allow for the planting of corn, oil palms or pulpwood trees. Once the water has drained away, bacteria and fungi then break down the carbon in the peat and turn it into CO₂ and methane. Meanwhile, the remainder of the solid matter in the peat starts to move downwards, in a process known as subsidence. Peat comprises 90 per cent water, so this is one of the most alarming consequences of peatland clearances. ‘In the tropics, peat subsides at about four centimetres a year, so within half a century, very large landscapes on Sumatra and Borneo will become flooded as the peat drops below water level,’ says Silvius. ‘It’s a huge catastrophe that’s in preparation. Some provinces will lose 40 per cent of their landmass.’</p><p>F. And while these industries affect wetlands in ways that can easily be documented, Dr Dave Tickner of the WWFN believes that more subtle impacts can be even more devastating. ‘Sediment run-off and fertilizers can be pretty invisible,’ says Tickner. ‘Over-extraction of water is equally invisible. You do get shock stories about rivers running red, or even catching fire, but there’s seldom one big impact that really hurts a wetland.’ Tickner does not blame anyone for deliberate damage, however. ‘I’ve worked on wetland issues for 20 years and have never met anybody who wanted to damage a wetland,’ he says. ‘It isn’t something that people generally set out to do. Quite often, the effects simply come from people trying to make a living.’</p><p>G. Silvius also acknowledges the importance of income generation. ‘It’s not that we just want to restore the biodiversity of wetlands – which we do – but we recognise there’s a need to provide an income for local people.’ This approach is supported by IWMI. ‘The idea is that people in a developing country will only protect wetlands if they value and profit from them,’ says McCartney. ‘For sustainability, it’s essential that local people are involved in wetland planning and decision making and have clear rights to use wetlands.’</p><p>H. The fortunes of wetlands would be improved, Silvius suggests, if more governments recognized their long-term value. ‘Different governments have different attitudes,’ he says, and goes on to explain that some countries place a high priority on restoring wetlands, while others still deny the issue. McCartney is cautiously optimistic, however. ‘Awareness of the importance of wetlands is growing,’ he says. ‘It’s true that wetland degradation still continues at a rapid pace, but my impression is that things are slowly changing.’</p><p>* peat: a brown deposit formed by the partial decomposition of vegetation in wet acidic conditions, often cut out and dried for use as fuel</p>',
    2
);
SET @passage2_id = LAST_INSERT_ID();

-- B. Group 3 (Matching Information)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 14-17: Reading Passage 2 has eight paragraphs, A–H. Which paragraph contains the following information?', 1);
SET @group3_id = LAST_INSERT_ID();
-- Type 5: Matching Information
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group3_id, 5, 'reference to the need to ensure that inhabitants of wetland regions continue to benefit from them', 'G', 14),
(@group3_id, 5, 'the proportion of wetlands which have already been lost', 'A', 15),
(@group3_id, 5, 'reference to the idea that people are beginning to appreciate the value of wetlands', 'H', 16),
(@group3_id, 5, 'mention of the cultural significance of wetlands', 'B', 17);

-- C. Group 4 (Sentence Completion - ONE WORD ONLY)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 18-22: Complete the sentences below. Choose ONE WORD ONLY from the passage for each answer.', 2);
SET @group4_id = LAST_INSERT_ID();
-- Type 7: Sentence Completion
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group4_id, 7, 'Peatlands which have been drained begin to release 18.…………………… instead of storing it.', 'carbon', 18),
(@group4_id, 7, 'Once peatland areas have been cleared, 19.…………………… are more likely to occur.', 'fires', 19),
(@group4_id, 7, 'Clearing peatland forests to make way for oil palm plantations destroys the 20.…………………… of the local environment.', 'biodiversity', 20),
(@group4_id, 7, 'Water is drained out of peatlands through the 21.…………………… which are created by logging companies.', 'ditches', 21),
(@group4_id, 7, 'Draining peatlands leads to 22.…………………… : a serious problem which can eventually result in coastal flooding and land loss.', 'subsidence', 22);

-- D. Group 5 (Matching Features - Experts)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage2_id, 'Questions 23-26: Look at the following statements (Questions 23–26) and the list of experts below. Match each statement with the correct expert, A–D.\nList of Experts: A. Matthew McCartney, B. Pieter van Eijk, C. Marcel Silvius, D. Dave Tickner', 3);
SET @group5_id = LAST_INSERT_ID();
-- Type 6: Matching Features
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group5_id, 6, 'Communities living in wetland regions must be included in discussions about the future of these areas.', 'A', 23),
(@group5_id, 6, 'Official policies towards wetlands vary from one nation to the next.', 'C', 24),
(@group5_id, 6, 'People cause harm to wetlands without having any intention to do so.', 'D', 25),
(@group5_id, 6, 'Initiatives to reverse environmental damage need not be complex.', 'B', 26);


-- =================================================================
-- 🗣️ PASSAGE 3: IS THE ERA OF ARTIFICIAL SPEECH TRANSLATION UPON US?
-- =================================================================

-- A. Insert Passage Text (HTML Formatted)
INSERT INTO reading_passage (test_id, title, passage_text, passage_order) VALUES
(
    @test_id,
    'Is the era of artificial speech translation upon us?',
    '<p>Once the stuff of science fiction, technology that enables people to talk using different languages is now here. But how effective is it?</p><p>Noise, Alex Waibel tells me, is one of the major challenges that artificial speech translation has to meet. A device may be able to recognize speech in a laboratory, or a meeting room, but will struggle to cope with the kind of background noise I can hear in my office surrounding Professor Waibel as he speaks to me from Kyoto station in Japan. I’m struggling to follow him in English, on a scratchy line that reminds me we are nearly 10,000 kilometers apart – and that distance is still an obstacle to communication even if you’re speaking the same language, as we are. We haven’t reached the future yet. If we had, Waibel would have been able to speak more comfortably in his native German and I would have been able to hear his words in English.</p><p>At Karlsruhe Institute of Technology, where he is a professor of computer science, Waibel and his colleagues already give lectures in German that their students can follow in English via an electronic translator. The system generates text that students can read on their laptops or phones, so the process is somewhat similar to subtitling. It helps that lecturers speak clearly, don’t have to compete with background chatter, and say much the same thing each year.</p><p>The idea of artificial speech translation has been around for a long time. Douglas Adams’ science fiction novel, The Hitchhiker’s Guide to the Galaxy, published in 1979, featured a life form called the ‘Babel fish’ which, when placed in the ear, enabled a listener to understand any language in the universe. It came to represent one of those devices that technology enthusiasts dream of long before they become practically realizable, like TVs flat enough to hang on walls: objects that we once could only dream of having but that are now commonplace. Now devices that look like prototype Babel fish have started to appear, riding a wave of advances in artificial translation and voice recognition.</p><p>At this stage, however, they seem to be regarded as eye-catching novelties rather than steps towards what Waibel calls ‘making a language-transparent society.’ They tend to be domestic devices or applications suitable for hotel check-ins, for example, providing a practical alternative to speaking traveler’s English. The efficiency of the translator is less important than the social function. However, ‘Professionals are less inclined to be patient in a conversation,’ founder and CEO at Waverly Labs, Andrew Ochoa, observes. To redress this, Waverly is now preparing a new model for professional applications, which entails performance improvements in speech recognition, translation accuracy and the time it takes to deliver the translated speech.</p><p>For a conversation, both speakers need to have devices called Pilots (translator earpieces) in their ears. ‘We find that there’s a barrier with sharing one of the earphones with a stranger,’ says Ochoa. That can’t have been totally unexpected. The problem would be solved if earpiece translators became sufficiently prevalent that strangers would be likely to already have their own in their ears. Whether that happens, and how quickly, will probably depend not so much on the earpieces themselves, but on the prevalence of voice-controlled devices and artificial translation in general.</p><p>Waibel highlights the significance of certain Asian nations, noting that voice translation has really taken off in countries such as Japan with a range of systems. There is still a long way to go, though. A translation system needs to be simultaneous, like the translator’s voice speaking over the foreign politician being interviewed on the TV, rather than in sections that oblige speakers to pause after every few remarks and wait for the translation to be delivered. It needs to work offline, for situations where internet access isn’t possible, and to address apprehensions about the amount of private speech data accumulating in the cloud, having been sent to servers for processing.</p><p>Systems not only need to cope with physical challenges such as noise, they will also need to be socially aware by addressing people in the right way. Some cultural traditions demand solemn respect for academic status, for example, and it is only polite to respect this. Etiquette-sensitive artificial translators could relieve people of the need to know these differing cultural norms. At the same time, they might help to preserve local customs, slowing the spread of habits associated with international English, such as its readiness to get on first-name terms.</p><p>Professors and other professionals will not outsource language awareness to software, though. If the technology matures into seamless, ubiquitous artificial speech translation, it will actually add value to language skills. Whether it will help people conduct their family lives or relationships is open to question—though one noteworthy possibility is that it could overcome the language barriers that often arise between generations after migration, leaving children and their grandparents without a shared language.</p><p>Whatever uses it is put to, though, it will never be as good as the real thing. Even if voice-morphing technology simulates the speaker’s voice, their lip movements won’t match, and they will look like they are in a dubbed movie. The contrast will underline the value of shared languages, and the value of learning them. Sharing a language can promote a sense of belonging and community, as with the international scientists who use English as a lingua franca, where their predecessors used Latin. Though the practical need for a common language will diminish, the social value of sharing one will persist. And software will never be a substitute for the subtle but vital understanding that comes with knowledge of a language.</p>',
    3
);
SET @passage7_id = LAST_INSERT_ID(); -- Use @passage7_id to avoid collision if previous scripts were run

-- B. Group 7 (Multiple Choice)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage7_id, 'Questions 27-30: Choose the correct letter, A, B, C or D.', 1);
SET @group7_id = LAST_INSERT_ID();
-- Type 3: Multiple Choice
INSERT INTO reading_question (group_id, type_id, question_text, options, correct_answer, question_order) VALUES
(@group7_id, 3, 'What does the reader learn about the conversation in the first paragraph?', '{"A": "The speakers are communicating in different languages.", "B": "Neither of the speakers is familiar with their environment.", "C": "The topic of the conversation is difficult for both speakers.", "D": "Aspects of the conversation are challenging for both speakers."}', 'D', 27),
(@group7_id, 3, 'What assists the electronic translator during lectures at Karlsruhe Institute of Technology?', '{"A": "the repeated content of lectures", "B": "the students’ reading skills", "C": "the languages used", "D": "the lecturers’ technical ability"}', 'A', 28),
(@group7_id, 3, 'When referring to The Hitchhiker’s Guide to the Galaxy, the writer suggests that', '{"A": "the Babel fish was considered undesirable at the time.", "B": "this book was not seriously intending to predict the future.", "C": "artificial speech translation was not a surprising development.", "D": "some speech translation techniques are better than others."}', 'C', 29),
(@group7_id, 3, 'What does the writer say about sharing earpieces?', '{"A": "It is something people will get used to doing.", "B": "The reluctance to do this is understandable.", "C": "The equipment will be unnecessary in the future.", "D": "It is something few people need to worry about."}', 'B', 30);

-- C. Group 8 (Matching Features - Sentence Endings)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage7_id, 'Questions 31-34: Complete each sentence with the correct ending, A–F, below. A. but there are concerns about this, B. as systems do not need to conform to standard practices, C. but they are far from perfect, D. despite the noise issues, E. because translation is immediate, F. and have an awareness of good manners.', 2);
SET @group8_id = LAST_INSERT_ID();
-- Type 6: Matching Features
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group8_id, 6, 'Speech translation methods are developing fast in Japan', 'C', 31),
(@group8_id, 6, 'TV interviews that use translation voiceover methods are successful', 'E', 32),
(@group8_id, 6, 'Future translation systems should address people appropriately', 'F', 33),
(@group8_id, 6, 'Users may be able to maintain their local customs', 'B', 34);

-- D. Group 9 (Yes/No/Not Given)
INSERT INTO reading_question_group (passage_id, instructions, group_order)
VALUES (@passage7_id, 'Questions 35-40: Do the following statements agree with the views of the writer in Reading Passage 3? YES/NO/NOT GIVEN', 3);
SET @group9_id = LAST_INSERT_ID();
-- Type 2: Yes/No/Not Given
INSERT INTO reading_question (group_id, type_id, question_text, correct_answer, question_order) VALUES
(@group9_id, 2, 'Language translation systems will be seen as very useful throughout the academic and professional worlds.', 'NO', 35),
(@group9_id, 2, 'The overall value of automated translation to family life is yet to be shown.', 'YES', 36),
(@group9_id, 2, 'Automated translation could make life more difficult for immigrant families.', 'NO', 37),
(@group9_id, 2, 'Visual aspects of language translation are being considered by scientists.', 'NOT GIVEN', 38),
(@group9_id, 2, 'International scientists have found English easier to translate into other languages than Latin.', 'NOT GIVEN', 39),
(@group9_id, 2, 'As far as language is concerned, there is a difference between people’s social and practical needs.', 'YES', 40);