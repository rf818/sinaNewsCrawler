create table LINK_TO_BE_PROCESSED
(
    link varchar(1000)
);
create table LINK_ALREADY_PROCESSED
(
    link varchar(1000)
);
create table NEWS
(
    id          bigint primary key auto_increment,
    url         varchar(1000),
    title       text,
    content     text,
    created_at  timestamp default now(),
    modified_at timestamp default now()
) default charset = utf8mb4;
