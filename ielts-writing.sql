-- 1️⃣ Create database and use it
CREATE DATABASE IF NOT EXISTS ieltsdb;
USE ieltsdb;

-- 2️⃣ Create table
CREATE TABLE IF NOT EXISTS ielts_writing_test (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    task1_link VARCHAR(600),
    task1_question TEXT,
    task2_question TEXT
);

-- 3️⃣ Insert all tests (only run once)
INSERT INTO ielts_writing_test (task1_link, task1_question, task2_question) VALUES
('https://drive.google.com/file/d/1lGgXmdr8iCrSGLyaknlyQYDzFQ5u4p9g/view?usp=drive_link',
 'The chart shows components of GDP in the UK from 1992 to 2000. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Some people think that the government is wasting money on the arts and that this money could be better spent elsewhere. To what extent do you agree with this view?'),

('https://drive.google.com/file/d/18ppDafsRVX19HEFjUsCDPcWwUfItWgxn/view?usp=drive_link',
 'The pie chart shows the amount of money that a children s charity located in the USA spent and received in one year. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Art is considered an essential part of all cultures throughout the world. However, these days fewer and fewer people appreciate art and turn their focus to science, technology and business. Why do you think that is? What could be done to encourage more people to take interest in the arts? (2020)'),

('https://drive.google.com/file/d/1-_utQfpBZkXCPzzgsnJ9-NMqReim2L7m/view?usp=drive_link',
 'The table shows the proportions of pupils attending four secondary school types between 2000 and 2009. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Some of the methods used in advertising are unethical and unacceptable in today’s society. To what extent do you agree with this view? Give reasons for your answer and include any relevant examples from your own experience or knowledge.'),

('https://drive.google.com/file/d/1M-St9ntHZrGUw-C84Y6aUcdjxvZtFOO9/view?usp=drive_link',
 'The diagram illustrates the process that is used to manufacture bricks for the building industry. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Currently there is a trend towards the use of alternative forms of medicine. However, at best these methods are ineffective, and at worst they may be dangerous. To what extent do you agree with this statement?'),

('https://drive.google.com/file/d/1bKyWibfAyfAKnliYUZ3LguyWdMI6tvGy/view?usp=drive_link',
 'Below is a map of the city of Brandfield. City planners have decided to build a new shopping mall for the area, and two sites, S1 and S2 have been proposed. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Some people believe the aim of university education is to help graduates get better jobs. Others believe there are much wider benefits of university education for both individuals and society. Discuss both views and give your opinion.'),
 ('https://drive.google.com/file/d/1W0antaIH7Vu9O-Trr5ZsYhrSotgirR_2/view?usp=drive_link',
 'The bar chart shows the scores of teams A, B and C over four different seasons. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'In the last 20 years there have been significant developments in the field of information technology (IT), for example the World Wide Web and communication by email. However, future developments in IT are likely to have more negative effects than positive. To what extent do you agree with this view?'),

('https://drive.google.com/file/d/1kw1SymzFeoFVQXJoaNtI5fkcVng9lEgZ/view?usp=drive_link',
 'The pie charts show the electricity generated in Germany and France from all sources and renewables in the year 2009. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Some people think that the best way to reduce crime is to give longer prison sentences. Others, however, believe there are better alternative ways of reducing crime. Discuss both views and give your opinion.'),

('https://drive.google.com/file/d/1AdDbkR7HFR-hGkZvMGjnxDtbDfcSX58p/view?usp=drive_link',
 'The chart shows British Emigration to selected destinations between 2004 and 2007. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Government investment in the arts, such as music and theatre, is a waste of money. Governments must invest this money in public services instead. To what extent do you agree with this statement?'),

('https://drive.google.com/file/d/13-3jLJCawOVbm9j7tDMnvWQ-FhhevfhZ/view?usp=drive_link',
 'The line graph shows visits to and from the UK from 1979 to 1999, and the bar graph shows the most popular countries visited by UK residents in 1999. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Overpopulation of urban areas has led to numerous problems. Identify one or two serious ones and suggest ways that governments and individuals can tackle these problems.'),

('https://drive.google.com/file/d/1TOJey2Ql1ID0kPDXOSTK9ryqgzCW40gw/view?usp=drive_link',
 'The line graph shows thefts per thousand vehicles in four European countries between 1990 and 1999. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'Levels of youth crime are increasing rapidly in most cities around the world. What are the reasons for this, and suggest some solutions.'),
 
 ('https://drive.google.com/file/d/1FetKgnvwfv9X5MgrjVPXIYhz93bMZopW/view?usp=drive_link',
 'The pie chart shows the percentage of persons arrested in the five years ending 1994 and the bar chart shows the most recent reasons for arrest. Summarize the information by selecting and reporting the main features and make comparisons where relevant. Write at least 150 words.',
 'In order to solve traffic problems, governments should tax private car owners heavily and use the money to improve public transportation. What are the advantages and disadvantages of such a solution?'),

('https://drive.google.com/file/d/10zIftvQM7H-qbIze9uVNsb9ibtxcErbN/view?usp=drive_link',
 'The bar chart below shows the number of cars sold by three different car manufacturers (Toyota, Ford, and Honda) in four different regions (North America, Europe, Asia, and South America) in 2023. Summarize the information by selecting and reporting the main features and make comparisons where relevant.',
 'The percentage of overweight children in western society has increased by almost 20% in the last ten years. Discuss the causes and effects of this disturbing trend.'),

('https://drive.google.com/file/d/1-PRI6abGSyCuLN-te8z6yyEe4UQbO346/view?usp=drive_link',
 'The line chart below shows the number of international students enrolled in four different universities in Australia from 2000 to 2015. Summarize the information by selecting and reporting the main features and make comparisons where relevant.',
 'A growing number of people feel that animals should not be exploited by people and that they should have the same rights as humans, while others argue that humans must employ animals to satisfy their various needs, including uses for food and research. Discuss both views and give your opinion.'),

('https://drive.google.com/file/d/1vtDx5ZEeK9O4TBRJTmhvSbJT8BYHrf--/view?usp=drive_link',
 'The table below shows how patients evaluated different services at three health centres. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Examine the arguments in favour of and against animal experiments, and come to a conclusion on this issue.'),

('https://drive.google.com/file/d/1-fF4UTUkge_yXjF9SWKRP-USFXnF2a4D/view?usp=drive_link',
 'The graph below shows the average monthly change in the prices of three metals during 2014. Summarise the information by selecting and reporting the main features and make comparisons where relevant.',
 'Do the dangers derived from the use of chemicals in food production and preservation outweigh the advantages?'),

('https://drive.google.com/file/d/1FO7LcjpVyXEJmswFlRkDw5Zq5W76dH7Z/view?usp=drive_link',
 'The chart shows the average number of hours each day that Chinese, American, Turkish and Brazilian tourists spent doing leisure activities while on holiday in Greece in August 2019.',
 'Many old buildings protected by law are part of a nation’s history. Some people think they should be knocked down and replaced by new ones. How important is it to maintain old buildings? Should history stand in the way of progress?'),

('https://drive.google.com/file/d/1t7itr6r6UOKouQQcilvb1SkadHkHRjhb/view?usp=drive_link',
 'The pie charts below compare the proportion of energy capacity in gigawatts (GW) in 2015 with the predictions for 2040. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'According to a recent study, the more time people use the Internet, the less time they spend with real human beings. Some people say that instead of seeing the Internet as a way of opening up new communication possibilities worldwide, we should be concerned about the effect this is having on social interaction. How far do you agree with this opinion?'),

('https://drive.google.com/file/d/1p_X7OBKjFahHrX6_Wmuchd4HMAs1lWuv/view?usp=drive_link',
 'The charts give information about employment in the UK in 1998 and 2012. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'As people live longer and longer, the idea of cloning human beings in order to provide spare parts is becoming a reality. The idea horrifies most people, yet it is no longer mere science fiction. To what extent do you agree with such a procedure? Have you any reservations?'),

('https://drive.google.com/file/d/1dpD2PI-6pAP0z58kgnkZiRojCEbP67FL/view?usp=drive_link',
 'The bar chart and table show information about students from abroad studying in four English-speaking countries in 2004 and 2012. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'These days it is much easier for many people to travel to different countries for tourism than in the past. Do the advantages of this development outweigh the disadvantages?'),

('https://drive.google.com/file/d/1GnyrSWsntWC8ulAolxYVpryLo6meFLfY/view?usp=drive_link',
 'The chart below shows the proportion of businesses making e-commerce purchases by industry in Canada between 2015 and 2019. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Scientific developments in farming always bring major benefits. To what extent do you agree or disagree with this statement?'),
 
 ('https://drive.google.com/file/d/1MOfL7aD33WENyE_VecQ5VxsL1qWCRa53/view?usp=drive_link',
 'The bar chart shows the percentages of the Canadian workforce in five major industries in 1850 and 2020. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'In many countries around the world, rural people are moving to cities, so the population in the countryside is decreasing. Do you think this is a positive or a negative development?'),

('https://drive.google.com/file/d/1-xMU7JQuIyqSOz8s1Kp_9Cq-U66ZgQ49/view?usp=drive_link',
 'The maps show Pacific Railway Station in 1998 and now. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Some university students want to learn about other subjects in addition to their main subjects. Others believe it is more important to give all their time and attention to studying for a qualification. Discuss both these views and give your own opinion.'),

('https://drive.google.com/file/d/1xGVDfgt8X-mYprMjZytI5Q82_6vSUCJ1/view?usp=drive_link',
 'The graph shows data about the annual earnings of three bakeries in Calgary, 2000-2010. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Some people believe that social media sites, such as Facebook or Twitter, have a negative impact on young people and their ability to form personal relationships. Others believe that these sites bring people together in a beneficial way.'),

('https://drive.google.com/file/d/1AbSuv1Wjlk1YTs8pi-uhj94oEqw-L-7V/view?usp=drive_link',
 'The maps below show changes in the city of Nelson in recent times. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Homelessness is increasing in many major cities around the world. What do you think are the main causes of this problem and what measures could be taken to solve it?'),

('https://drive.google.com/file/d/1tMfT9jdY_7t4mSvZ-B4-SgTOnlVGtBYg/view?usp=drive_link',
 'The charts below show the favourite takeaways of people in Canada and the number of Indian restaurants in Canada between 1960 and 2015. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'People who live in large cities face a range of problems in their daily life. What are the main problems people in cities face, and how can these problems be tackled?'),

('https://drive.google.com/file/d/1jmH4cLT_3hp8SPQbmwp8uUeDZIUyzE-g/view?usp=drive_link',
 'The graph below shows the average daily sales of selected food items at the Brisk Café, by season. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Violence in playgrounds is increasing. However, it is important that parents should teach children not to hit back at bullies.'),

('https://drive.google.com/file/d/1_kfH8RCeL5eraPfddxf_531fWjQF6Pid/view?usp=drive_link',
 'The line chart below shows the results of a survey giving the reasons why people moved to the capital city of a particular country. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Some people think that the increase in international travel has a negative impact on the environment and should be restricted. To what extent do you agree or disagree with this opinion?'),

('https://drive.google.com/file/d/1szAPz0b9K06TJiB4XipiUBMLg8cgvwg4/view?usp=drive_link',
 'The graph gives information about the age of the population of Iceland between 1990 and 2020. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Some believe that people today have no interest in maintaining the traditional culture of their country or region. Others believe that it is still important to people that we preserve a traditional way of life. Discuss both these views and give your own opinion.'),

('https://drive.google.com/file/d/1HfqXhDHV_AcxO3hT57XYkargBGy3sapU/view?usp=drive_link',
 'The table below shows the estimated literacy rates by region and gender for 2000-2004. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'The position of women has changed a great deal in many societies over the past 50 years. But these societies cannot claim to have achieved gender equality. To what extent do you agree or disagree?'),

('https://drive.google.com/file/d/1Ca-Nb1p4Q4agUGcLu2xAjFmtvrc6CX6b/view?usp=drive_link',
 'The chart gives information on the percentage of women going into higher education in five countries for the years 1980 and 2015. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Many young people choose to take a year out between finishing school and starting university in order to gain work experience or to travel. The experience of non-academic life this offers benefits the individual when they return to education. To what extent do you agree or disagree?'),
 
 ('https://drive.google.com/file/d/1G3f0j0AKFzLH3yp8akG_WuRH1XQnq2Qc/view?usp=drive_link',
 'The chart and graph below give information about participants who have entered the Olympics since it began. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Investment in local amenities such as leisure centres is the best way for the government to foster a good community spirit. To what extent do you agree or disagree? What other measures do you think might be effective?'),

('https://drive.google.com/file/d/1d8Mq56-0PzLH7pRXI_cz3DRQpp291CNQ/view?usp=drive_link',
 'The graph gives information about male and female gym membership between 1980 and 2010. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'What distinguishes young people from their parents or grandparents generation is a lack of physical exercise. Today’s generation are spending far too long playing computer games, chatting aimlessly on social networking sites or simply watching TV, and too little time being active. To what extent do you agree or disagree?'),

('https://drive.google.com/file/d/1bML6z8dTGroE1Q3ADxY_Rd_QBkYzv0_M/view?usp=drive_link',
 'The pie charts compare ways of accessing the news in Canada and Australia. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Nowadays, experience is more valued in the workplace than knowledge in many countries. Do you think the advantages of this outweigh the disadvantages?'),

('https://drive.google.com/file/d/1lkxWZnXydeCut4rBJ0tYJiGu6UvYAE0K/view?usp=drive_link',
 'The maps below show the town of Langley in 1910 and 1950. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Fewer students are studying science at school and university, favouring more computer based subjects instead. Is this a positive or negative development? What are the reasons for this?'),

('https://drive.google.com/file/d/1rPQHeuXyBWFS4mInAD-1ra6OPCz8w6-m/view?usp=drive_link',
 'The table below shows the results of surveys in 2005, 2010 and 2015 about McGill University. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Tourism has increased so much over the last 50 years that it is having a mainly negative impact on local inhabitants and the environment. However, others claim that it is good for the economy. Discuss the advantages and disadvantages of tourism and give your own opinion.'),

('https://drive.google.com/file/d/1xmsFwIIYwFSvJ4U2Vu1djTDUmru99D2-/view?usp=drive_link',
 'The diagram below shows the recycling process of aluminium cans. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Overpopulation is the world’s most serious environmental problem. To what extent do you agree or disagree with this statement?'),

('https://drive.google.com/file/d/1Wv4wfXMbIXUGg2pRUAFy33VnfHcYSc6w/view?usp=drive_link',
 'The table below gives information about student enrolments at Manchester University in 1937, 1967 and 2017. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Should the international community do more to tackle the threat of global warming?'),

('https://drive.google.com/file/d/1KGa6tZWQr2sZ_70kGPvDJV1ZECfuUEH3/view?usp=drive_link',
 'The table below gives information about UK independent films. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'Genetic engineering is a dangerous trend. It should be limited. To what extent do you agree?'),

('https://drive.google.com/file/d/1JFE7aLUgoagUaFqLcZS5bzbnNLjgX795/view?usp=drive_link',
 'The maps below show the changes that have taken place at Queen Mary Hospital since its construction in 1960. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'A government’s role is only to provide defence capability and urban infrastructure (roads, water supplies, etc.). All other services (education, health, social security) should be provided by private groups or individuals in the community.'),

('https://drive.google.com/file/d/18c58tkes4eUhppzHfeDi3lH-mAXOByg7/view?usp=drive_link',
 'The diagram below shows the production of steam using a gas cooled nuclear reactor. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.',
 'In many countries, very few young people read newspapers or follow the news on TV. What do you think are the causes of this? What solutions can you suggest?');