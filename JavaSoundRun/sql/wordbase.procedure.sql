delimiter //

drop procedure  if exists Solitude //
-- word, idiom and proverbe will be separated into three table.
create procedure Solitude()
begin

declare word varchar(20) CHARACTER SET utf8 COLLATE utf8_bin;
declare count_idiom bigint unsigned;
declare count_proverbe bigint unsigned;
declare idiom_proverbe bigint unsigned;
declare total bigint unsigned;
declare miss int;
declare foreach_idiom cursor for 
    select A.CONTENT from IDIOM_LIST_LEVEL_0 as A join WORD_LIST_LEVEL_0 as B on A.CONTENT=B.CONTENT;
declare foreach_proverbe cursor for 
    select A.CONTENT from PROVERBE_LIST_LEVEL_0 as A join WORD_LIST_LEVEL_0 as B on A.CONTENT=B.CONTENT;

declare foreach_idiom_proverbe cursor for
    select A.CONTENT from IDIOM_LIST_LEVEL_0 as A join PROVERBE_LIST_LEVEL_0 as B on A.CONTENT=B.CONTENT;

declare continue handler for not found set  miss = 1;

set miss = 0;
set count_idiom = 0;
set count_proverbe = 0;
set total = 0;
set idiom_proverbe = 0;

open foreach_idiom;

repeat

    fetch foreach_idiom into word;
    
    if exists (select 1 from WORD_LIST_LEVEL_0 where CONTENT= word) 
    then
        delete from WORD_LIST_LEVEL_0 where CONTENT = word;
        insert into TRASH (CONTENT, WHEN_DELETE) values (word, now());

        set count_idiom = count_idiom+1;
    end if;

    set total = total+1;

until miss
end repeat;
close foreach_idiom;

set miss = 0;

open foreach_proverbe;

repeat

    fetch foreach_proverbe into word;
    
    if exists (select 1 from WORD_LIST_LEVEL_0 where CONTENT= word) 
    then
        delete from WORD_LIST_LEVEL_0 where CONTENT = word;
        insert into TRASH (CONTENT, WHEN_DELETE) values (word, now());

        set count_proverbe = count_proverbe+1;
    end if;
    
    set total = total+1;
until miss
end repeat;

close foreach_proverbe;

set miss = 0;

open foreach_idiom_proverbe;

repeat

    fetch foreach_idiom_proverbe into word;

    if exists (select 1 from IDIOM_LIST_LEVEL_0 where CONTENT= word) 
    then
        delete from IDIOM_LIST_LEVEL_0 where CONTENT = word;
        insert into TRASH (CONTENT, WHEN_DELETE) values (word, now());

        set idiom_proverbe = idiom_proverbe+1;
    end if;
    
    set total = total+1;

until miss
end repeat;

close foreach_idiom_proverbe;

select count_idiom, count_proverbe, idiom_proverbe, total;
end //



drop procedure  if exists IsPhrase //

create procedure IsPhrase( 
    in phrase varchar(20) CHARACTER SET utf8 COLLATE utf8_bin, 
    out exist int)
begin

declare res int;
set res = 0;
select exists 
    (
        select 1 from IDIOM_LIST_LEVEL_0 as a 
        where a.CONTENT=phrase
    ) as EXISTENCE into res;

if res= 1
then
    select res into exist;
end if;
if res = 0
then  
    select exists 
        (
            select 1 from PROVERBE_LIST_LEVEL_0 as b 
            where  b.CONTENT=phrase
        ) as EXISTENCE into res;
        
    if res = 1
    then 
        select res into exist;
    end if;

end if;

if res = 0
then
    select exists 
        (
            select 1 from WORD_LIST_LEVEL_0 as c 
            where c.CONTENT=phrase
        ) as EXISTENCE into res;
        
    if res = 1
    then 
        select res into exist;
    end if;
    
end if;

if res = 0
then 
    select res into exist;
end if;


end //

drop procedure  if exists IsAux  //

create procedure IsAux(
    in aux varchar(5) CHARACTER SET utf8 COLLATE utf8_bin,
    out exist int
    )
begin

select exists (
    select 1 from CHARACTER_LIST_LEVEL_5
    where LITERAL = aux ) into exist;


end //


drop procedure if exists GetCharacter0  //

create procedure GetCharacter0()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_0
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_0.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetCharacter1  //

create procedure GetCharacter1()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_1
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_1.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetCharacter2  //

create procedure GetCharacter2()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_2
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_2.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetCharacter3  //

create procedure GetCharacter3()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_3
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_3.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetCharacter4  //

create procedure GetCharacter4()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_4
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_4.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetCharacter5  //

create procedure GetCharacter5()
begin
(select 'LITERAL', 'SPELL_1', 'UNICODE') union (
select LITERAL, SPELL_1, UNICODE from CHARACTER_LIST_LEVEL_5
    into outfile '/tmp/dbx_data/CHARACTER_LIST_LEVEL_5.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //


drop procedure if exists GetIdiom  //

create procedure GetIdiom()
begin
(select 'CONTENT') union 
(select CONTENT from IDIOM_LIST_LEVEL_0
    into outfile '/tmp/dbx_data/IDIOM_LIST_LEVEL_0.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetProverbe  //

create procedure GetProverbe()
begin
(select 'CONTENT') union 
(select CONTENT from PROVERBE_LIST_LEVEL_0
    into outfile '/tmp/dbx_data/PROVERBE_LIST_LEVEL_0.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetWord0  //

create procedure GetWord0()
begin
(select 'CONTENT') union
(select CONTENT from WORD_LIST_LEVEL_0
    into outfile '/tmp/dbx_data/WORD_LIST_LEVEL_0.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetWord1  //

create procedure GetWord1()
begin
(select 'CONTENT', 'STRING_1') union 
(select CONTENT, STRING_1 from WORD_LIST_LEVEL_1
    into outfile '/tmp/dbx_data/WORD_LIST_LEVEL_1.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end  //

drop procedure if exists GetWaveFile  //

create procedure GetWaveFile() 
begin
(select 'HASH_1', 'PINYIN', 'WAVEFILE') union
(select HASH_1, PINYIN, WAVEFILE from CHARACTER_WAVE_LIST_LEVEL_0
    where char_length(WAVEFILE) >0
    into outfile '/tmp/dbx_data/CHARACTER_WAVE_LIST_LEVEL_0.dbx'
    FIELDS 
    TERMINATED BY ','
    ENCLOSED BY ''
    LINES TERMINATED BY '\n');
end //

drop procedure if exists GetAllData  //

create procedure GetAllData()
begin
call GetCharacter0();
call GetCharacter5();
call GetIdiom();
call GetProverbe();
call GetWaveFile();
call GetWord0();
call GetWord1();
end  //

delimiter ;WORD_MAJOR_INFO