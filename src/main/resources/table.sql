CREATE TABLE `cpbpc_system_config`
(
    `id`    int         NOT NULL AUTO_INCREMENT,
    `key`   varchar(20) NOT NULL,
    `value` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;


insert into cpbpc_system_config (`key`, VALUE)
values ('rpg_process_id', '10771');


CREATE TABLE cpbpc_abbreviation
(
    id            INT auto_increment NOT NULL,
    complete_form varchar(100) NOT NULL,
    short_form    varchar(100) NOT NULL,
    `group`       varchar(100) NOT NULL,
    CONSTRAINT cpbpc_abbreviation_PK PRIMARY KEY (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE cpbpc_abbreviation MODIFY COLUMN `group` varchar (100) DEFAULT 'bible';

insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Genesis', 'Genesis', 'bible'),
       ('Genesis', 'Gen', 'bible'),
       ('Genesis', 'Gn', 'bible'),
       ('Genesis', 'Ge', 'bible'),
       ('Exodus', 'Exodus', 'bible'),
       ('Exodus', 'Ex', 'bible'),
       ('Exodus', 'Exod', 'bible'),
       ('Exodus', 'Exo', 'bible'),
       ('Leviticus', 'Leviticus', 'bible'),
       ('Leviticus', 'Lev', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Leviticus', 'Le', 'bible'),
       ('Leviticus', 'Lv', 'bible'),
       ('Numbers', 'Numbers', 'bible'),
       ('Numbers', 'Num', 'bible'),
       ('Numbers', 'Nu', 'bible'),
       ('Numbers', 'Nm', 'bible'),
       ('Numbers', 'Nb', 'bible'),
       ('Deuteronomy', 'Deuteronomy', 'bible'),
       ('Deuteronomy', 'Deut', 'bible'),
       ('Deuteronomy', 'De', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Deuteronomy', 'Dt', 'bible'),
       ('Joshua', 'Joshua', 'bible'),
       ('Joshua', 'Josh', 'bible'),
       ('Joshua', 'Jos', 'bible'),
       ('Joshua', 'Jsh', 'bible'),
       ('Judges', 'Judges', 'bible'),
       ('Judges', 'Judg', 'bible'),
       ('Judges', 'Jdg', 'bible'),
       ('Judges', 'Jg', 'bible'),
       ('Judges', 'Jdgs', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Ruth', 'Ruth', 'bible'),
       ('Ruth', 'Rth', 'bible'),
       ('Ruth', 'Ru', 'bible'),
       ('First Samuel', '1 Samuel', 'bible'),
       ('First Samuel', '1 Sam', 'bible'),
       ('First Samuel', '1Sam', 'bible'),
       ('First Samuel', '1 Sm', 'bible'),
       ('First Samuel', '1Sm', 'bible'),
       ('First Samuel', '1 Sa', 'bible'),
       ('First Samuel', '1Sa', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Samuel', '1S', 'bible'),
       ('First Samuel', '1 S', 'bible'),
       ('First Samuel', '1st Samuel', 'bible'),
       ('First Samuel', 'First Samuel', 'bible'),
       ('First Samuel', 'First Sam', 'bible'),
       ('First Samuel', '1st Sam', 'bible'),
       ('Second Samuel', '2 Samuel', 'bible'),
       ('Second Samuel', '2 Sam', 'bible'),
       ('Second Samuel', '2Sam', 'bible'),
       ('Second Samuel', '2 Sm', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Samuel', '2Sm', 'bible'),
       ('Second Samuel', '2 Sa', 'bible'),
       ('Second Samuel', '2Sa', 'bible'),
       ('Second Samuel', '2S', 'bible'),
       ('Second Samuel', '2 S', 'bible'),
       ('Second Samuel', '2nd Samuel', 'bible'),
       ('Second Samuel', 'Second Samuel', 'bible'),
       ('Second Samuel', 'Second Sam', 'bible'),
       ('Second Samuel', '2nd Sam', 'bible'),
       ('First Kings', '1 Kings', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Kings', '1Kings', 'bible'),
       ('First Kings', '1 Kgs', 'bible'),
       ('First Kings', '1Kgs', 'bible'),
       ('First Kings', '1 Ki', 'bible'),
       ('First Kings', '1Ki', 'bible'),
       ('First Kings', '1Kin', 'bible'),
       ('First Kings', '1 K', 'bible'),
       ('First Kings', '1K', 'bible'),
       ('First Kings', '1st Kings', 'bible'),
       ('First Kings', '1st Kgs', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Kings', 'First Kings', 'bible'),
       ('First Kings', 'First Kgs', 'bible'),
       ('Second Kings', '2 Kings', 'bible'),
       ('Second Kings', '2 Kgs', 'bible'),
       ('Second Kings', '2Kgs', 'bible'),
       ('Second Kings', '2 Kin', 'bible'),
       ('Second Kings', '2Kin', 'bible'),
       ('Second Kings', '2 Ki', 'bible'),
       ('Second Kings', '2Ki', 'bible'),
       ('Second Kings', '2 K', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Kings', '2K', 'bible'),
       ('Second Kings', '2nd Kings', 'bible'),
       ('Second Kings', 'Second Kings', 'bible'),
       ('Second Kings', 'Second Kgs', 'bible'),
       ('Second Kings', '2nd Kgs', 'bible'),
       ('Second Samuel', '2Samuel', 'bible'),
       ('First Samuel', '1Samuel', 'bible'),
       ('First Chronicles', '1 Chronicles', 'bible'),
       ('First Chronicles', '1Chronicles', 'bible'),
       ('First Chronicles', '1 Chron', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Chronicles', '1Chron', 'bible'),
       ('First Chronicles', '1 Chr', 'bible'),
       ('First Chronicles', '1Chr', 'bible'),
       ('First Chronicles', '1 Ch', 'bible'),
       ('First Chronicles', '1Ch', 'bible'),
       ('First Chronicles', '1st Chronicles', 'bible'),
       ('First Chronicles', '1st Chron', 'bible'),
       ('First Chronicles', 'First Chronicles', 'bible'),
       ('First Chronicles', 'First Chron', 'bible'),
       ('Second Chronicles', '2 Chronicles', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Chronicles', '2Chronicles', 'bible'),
       ('Second Chronicles', '2 Chron', 'bible'),
       ('Second Chronicles', '2Chron', 'bible'),
       ('Second Chronicles', '2 Chr', 'bible'),
       ('Second Chronicles', '2Chr', 'bible'),
       ('Second Chronicles', '2 Ch', 'bible'),
       ('Second Chronicles', '2Ch', 'bible'),
       ('Second Chronicles', '2nd Chronicles', 'bible'),
       ('Second Chronicles', '2nd Chron', 'bible'),
       ('Second Chronicles', 'Second Chronicles', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Chronicles', 'Second Chron', 'bible'),
       ('Ezra', 'Ezra', 'bible'),
       ('Ezra', 'Ezr', 'bible'),
       ('Ezra', 'Ez', 'bible'),
       ('Nehemiah', 'Nehemiah', 'bible'),
       ('Nehemiah', 'Neh', 'bible'),
       ('Nehemiah', 'Ne', 'bible'),
       ('Esther', 'Esther', 'bible'),
       ('Esther', 'Est', 'bible'),
       ('Esther', 'Esth', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Esther', 'Es', 'bible'),
       ('Job', 'Job', 'bible'),
       ('Job', 'Jb', 'bible'),
       ('Psalms', 'Psalms', 'bible'),
       ('Psalms', 'Ps', 'bible'),
       ('Psalms', 'Psalm', 'bible'),
       ('Psalms', 'Pslm', 'bible'),
       ('Psalms', 'Psa', 'bible'),
       ('Psalms', 'Psm', 'bible'),
       ('Psalms', 'Pss', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Proverbs', 'Proverbs', 'bible'),
       ('Proverbs', 'Prov', 'bible'),
       ('Proverbs', 'Pro', 'bible'),
       ('Proverbs', 'Prv', 'bible'),
       ('Proverbs', 'Pr', 'bible'),
       ('Ecclesiastes', 'Ecclesiastes', 'bible'),
       ('Ecclesiastes', 'Eccles', 'bible'),
       ('Ecclesiastes', 'Eccle', 'bible'),
       ('Ecclesiastes', 'Eccl', 'bible'),
       ('Ecclesiastes', 'Ecc', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Ecclesiastes', 'Ec', 'bible'),
       ('Song of Solomon', 'Song of Solomon', 'bible'),
       ('Song of Solomon', 'Song', 'bible'),
       ('Song of Solomon', 'Song of Songs', 'bible'),
       ('Song of Solomon', 'SOS', 'bible'),
       ('Song of Solomon', 'So', 'bible'),
       ('Isaiah', 'Isaiah', 'bible'),
       ('Isaiah', 'Isa', 'bible'),
       ('Isaiah', 'Is', 'bible'),
       ('Jeremiah', 'Jeremiah', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Jeremiah', 'Jer', 'bible'),
       ('Jeremiah', 'Je', 'bible'),
       ('Jeremiah', 'Jr', 'bible'),
       ('Lamentations', 'Lamentations', 'bible'),
       ('Lamentations', 'Lam', 'bible'),
       ('Lamentations', 'La', 'bible'),
       ('Ezekiel', 'Ezekiel', 'bible'),
       ('Ezekiel', 'Ezek', 'bible'),
       ('Ezekiel', 'Eze', 'bible'),
       ('Ezekiel', 'Ezk', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Daniel', 'Daniel', 'bible'),
       ('Daniel', 'Dan', 'bible'),
       ('Daniel', 'Dn', 'bible'),
       ('Daniel', 'Da', 'bible'),
       ('Hosea', 'Hosea', 'bible'),
       ('Hosea', 'Hos', 'bible'),
       ('Hosea', 'Ho', 'bible'),
       ('Joel', 'Joel', 'bible'),
       ('Joel', 'Jl', 'bible'),
       ('Amos', 'Amos', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Amos', 'Am', 'bible'),
       ('Obadiah', 'Obadiah', 'bible'),
       ('Obadiah', 'Obad', 'bible'),
       ('Obadiah', 'Ob', 'bible'),
       ('Jonah', 'Jonah', 'bible'),
       ('Jonah', 'Jnh', 'bible'),
       ('Jonah', 'Jon', 'bible'),
       ('Micah', 'Micah', 'bible'),
       ('Micah', 'Mic', 'bible'),
       ('Micah', 'Mc', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Nahum', 'Nahum', 'bible'),
       ('Nahum', 'Nah', 'bible'),
       ('Nahum', 'Na', 'bible'),
       ('Habakkuk', 'Habakkuk', 'bible'),
       ('Habakkuk', 'Hab', 'bible'),
       ('Habakkuk', 'Hb', 'bible'),
       ('Zephaniah', 'Zephaniah', 'bible'),
       ('Zephaniah', 'Zeph', 'bible'),
       ('Zephaniah', 'Zep', 'bible'),
       ('Zephaniah', 'Zp', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Haggai', 'Haggai', 'bible'),
       ('Haggai', 'Hag', 'bible'),
       ('Haggai', 'Hg', 'bible'),
       ('Zechariah', 'Zechariah', 'bible'),
       ('Zechariah', 'Zech', 'bible'),
       ('Zechariah', 'Zec', 'bible'),
       ('Zechariah', 'Zc', 'bible'),
       ('Malachi', 'Malachi', 'bible'),
       ('Malachi', 'Mal', 'bible'),
       ('Malachi', 'Ml', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Matthew', 'Matthew', 'bible'),
       ('Matthew', 'Matt', 'bible'),
       ('Matthew', 'Mt', 'bible'),
       ('Mark', 'Mark', 'bible'),
       ('Mark', 'Mrk', 'bible'),
       ('Mark', 'Mar', 'bible'),
       ('Mark', 'Mk', 'bible'),
       ('Mark', 'Mr', 'bible'),
       ('Luke', 'Luke', 'bible'),
       ('Luke', 'Luk', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Luke', 'Lk', 'bible'),
       ('John', 'John', 'bible'),
       ('John', 'Jon', 'bible'),
       ('John', 'Jhn', 'bible'),
       ('John', 'Jn', 'bible'),
       ('Acts', 'Acts', 'bible'),
       ('Acts', 'Act', 'bible'),
       ('Acts', 'Ac', 'bible'),
       ('Romans', 'Romans', 'bible'),
       ('Romans', 'Rom', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Romans', 'Ro', 'bible'),
       ('Romans', 'Rm', 'bible'),
       ('First Corinthians', '1 Corinthians', 'bible'),
       ('First Corinthians', '1Corinthians', 'bible'),
       ('First Corinthians', '1 Cor', 'bible'),
       ('First Corinthians', '1Cor', 'bible'),
       ('First Corinthians', '1 Co', 'bible'),
       ('First Corinthians', '1Co', 'bible'),
       ('First Corinthians', '1st Corinthians', 'bible'),
       ('First Corinthians', 'First Corinthians', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Corinthians', '2 Corinthians', 'bible'),
       ('Second Corinthians', '2Corinthians', 'bible'),
       ('Second Corinthians', '2 Cor', 'bible'),
       ('Second Corinthians', '2Cor', 'bible'),
       ('Second Corinthians', '2 Co', 'bible'),
       ('Second Corinthians', '12Co', 'bible'),
       ('Second Corinthians', '2nd Corinthians', 'bible'),
       ('Second Corinthians', 'Second Corinthians', 'bible'),
       ('Galatians', 'Galatians', 'bible'),
       ('Galatians', 'Gal', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Galatians', 'Ga', 'bible'),
       ('Ephesians', 'Ephesians', 'bible'),
       ('Ephesians', 'Eph', 'bible'),
       ('Ephesians', 'Ephes', 'bible'),
       ('Philippians', 'Philippians', 'bible'),
       ('Philippians', 'Phil', 'bible'),
       ('Philippians', 'Php', 'bible'),
       ('Philippians', 'Pp', 'bible'),
       ('Colossians', 'Colossians', 'bible'),
       ('Colossians', 'Col', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Colossians', 'Co', 'bible'),
       ('First Thessalonians', '1 Thessalonians', 'bible'),
       ('First Thessalonians', '1Thessalonians', 'bible'),
       ('First Thessalonians', '1 Thess', 'bible'),
       ('First Thessalonians', '1Thess', 'bible'),
       ('First Thessalonians', '1 Thes', 'bible'),
       ('First Thessalonians', '1Thes', 'bible'),
       ('First Thessalonians', '1 Th', 'bible'),
       ('First Thessalonians', '1Th', 'bible'),
       ('Second Thessalonians', '2 Thessalonians', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Thessalonians', '2Thessalonians', 'bible'),
       ('Second Thessalonians', '2 Thess', 'bible'),
       ('Second Thessalonians', '2Thess', 'bible'),
       ('Second Thessalonians', '2 Thes', 'bible'),
       ('Second Thessalonians', '2Thes', 'bible'),
       ('Second Thessalonians', '2 Th', 'bible'),
       ('Second Thessalonians', '2Th', 'bible'),
       ('First Thessalonians', '1st Thessalonians', 'bible'),
       ('First Thessalonians', '1st Thess', 'bible'),
       ('First Thessalonians', 'First Thessalonians', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Thessalonians', 'First Thess', 'bible'),
       ('Second Thessalonians', '2nd Thessalonians', 'bible'),
       ('Second Thessalonians', '2nd Thess', 'bible'),
       ('Second Thessalonians', 'Second Thessalonians', 'bible'),
       ('Second Thessalonians', 'Second Thess', 'bible'),
       ('First Timothy', '1 Timothy', 'bible'),
       ('First Timothy', '1Timothy', 'bible'),
       ('First Timothy', '1 Tim', 'bible'),
       ('First Timothy', '1Tim', 'bible'),
       ('First Timothy', '1 Ti', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Timothy', '1Ti', 'bible'),
       ('First Timothy', '1st Timothy', 'bible'),
       ('First Timothy', '1st Tim', 'bible'),
       ('Second Timothy', '2 Timothy', 'bible'),
       ('Second Timothy', '2Timothy', 'bible'),
       ('Second Timothy', '2 Tim', 'bible'),
       ('Second Timothy', '2Tim', 'bible'),
       ('Second Timothy', '2Ti', 'bible'),
       ('Second Timothy', '2 Ti', 'bible'),
       ('Second Timothy', '2nd Timothy', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Timothy', '2nd Tim', 'bible'),
       ('First Timothy', 'First Timothy', 'bible'),
       ('First Timothy', 'First Tim', 'bible'),
       ('Second Timothy', 'Second Timothy', 'bible'),
       ('Second Timothy', 'Second Tim', 'bible'),
       ('Titus', 'Titus', 'bible'),
       ('Titus', 'Tit', 'bible'),
       ('Titus', 'ti', 'bible'),
       ('Philemon', 'Philemon', 'bible'),
       ('Philemon', 'Philem', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Philemon', 'Phm', 'bible'),
       ('Philemon', 'Pm', 'bible'),
       ('Hebrews', 'Hebrews', 'bible'),
       ('Hebrews', 'Heb', 'bible'),
       ('James', 'James', 'bible'),
       ('James', 'Jas', 'bible'),
       ('James', 'Jm', 'bible'),
       ('First Peter', '1 Peter', 'bible'),
       ('First Peter', '1Peter', 'bible'),
       ('First Peter', '1 Pet', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First Peter', '1Pet', 'bible'),
       ('First Peter', '1 Pe', 'bible'),
       ('First Peter', '1Pe', 'bible'),
       ('First Peter', '1 Pt', 'bible'),
       ('First Peter', '1Pt', 'bible'),
       ('First Peter', '1 P', 'bible'),
       ('First Peter', '1P', 'bible'),
       ('First Peter', '1st Peter', 'bible'),
       ('First Peter', 'First Peter', 'bible'),
       ('Second Peter', '2 Peter', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Peter', '2Peter', 'bible'),
       ('Second Peter', '2 Pet', 'bible'),
       ('Second Peter', '2Pet', 'bible'),
       ('Second Peter', '2 Pe', 'bible'),
       ('Second Peter', '2Pe', 'bible'),
       ('Second Peter', '2 Pt', 'bible'),
       ('Second Peter', '2Pt', 'bible'),
       ('Second Peter', '2 P', 'bible'),
       ('Second Peter', '2P', 'bible'),
       ('Second Peter', '2nd Peter', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second Peter', 'Second Peter', 'bible'),
       ('First John', '1 John', 'bible'),
       ('First John', '1John', 'bible'),
       ('First John', '1 Jhn', 'bible'),
       ('First John', '1Jhn', 'bible'),
       ('First John', '1 Jn', 'bible'),
       ('First John', '1Jn', 'bible'),
       ('First John', '1 J', 'bible'),
       ('First John', '1J', 'bible'),
       ('First John', '1st John', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('First John', 'First John', 'bible'),
       ('Second John', '2 John', 'bible'),
       ('Second John', '2John', 'bible'),
       ('Second John', '2 Jhn', 'bible'),
       ('Second John', '2Jhn', 'bible'),
       ('Second John', '2 Jn', 'bible'),
       ('Second John', '2Jn', 'bible'),
       ('Second John', '2 J', 'bible'),
       ('Second John', '2J', 'bible'),
       ('Second John', '2nd John', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Second John', 'Second John', 'bible'),
       ('Third John', '3 John', 'bible'),
       ('Third John', '3John', 'bible'),
       ('Third John', '3 Jhn', 'bible'),
       ('Third John', '3Jhn', 'bible'),
       ('Third John', '3 Jn', 'bible'),
       ('Third John', '3Jn', 'bible'),
       ('Third John', '3 J', 'bible'),
       ('Third John', '3J', 'bible'),
       ('Third John', '3rd John', 'bible');
insert into cpbpc_abbreviation (COMPLETE_FORM, SHORT_FORM, `group`)
values ('Third John', 'Third John', 'bible'),
       ('Jude', 'Jude', 'bible'),
       ('Jude', 'Jud', 'bible'),
       ('Jude', 'Jd', 'bible'),
       ('Revelation', 'Revelation', 'bible'),
       ('Revelation', 'Rev', 'bible'),
       ('Revelation', 'Re', 'bible'),
       ('that is', 'i.e.', 'words'),
       ('for example', 'e.g.', 'words'),
       ('and so forth', 'etc.', 'words');
