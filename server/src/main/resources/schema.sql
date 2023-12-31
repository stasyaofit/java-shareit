DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS item_requests CASCADE;

create table if not exists USERS
(
    USER_ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    NAME    CHARACTER VARYING(40) not null,
    EMAIL   CHARACTER VARYING(64) not null,
    constraint UQ_USER_EMAIL unique (EMAIL),
    constraint USERS_PK
        primary key (USER_ID)
);

create table if not exists REQUESTS
(
    REQUEST_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    DESCRIPTION  CHARACTER VARYING(255) not null,
    REQUESTER_ID BIGINT not null,
    CREATED      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() not null,
    constraint  REQUESTS_PK
        primary key (REQUEST_ID),
    constraint ITEM_REQUESTS_USERS_USER_ID_FK
        foreign key (REQUESTER_ID) references USERS ON DELETE CASCADE
);

create table if not exists ITEMS
(
    ITEM_ID     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    ITEM_NAME   CHARACTER VARYING(128) not null,
    DESCRIPTION CHARACTER VARYING(200) not null,
    AVAILABLE   BOOLEAN                not null,
    OWNER_ID    BIGINT                 not null,
    REQUEST_ID  BIGINT                 ,
    constraint ITEMS_PK
        primary key (ITEM_ID),
    constraint "items_USERS_USER_ID_fk"
        foreign key (OWNER_ID) references USERS ON DELETE CASCADE
);

create table if not exists COMMENTS
(
    COMMENT_ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    TEXT       CHARACTER VARYING(1024)          not null,
    AUTHOR_ID  BIGINT                           not null,
    ITEM_ID    BIGINT                           not null,
    CREATED    TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() not null,
    constraint COMMENTS_PK
        primary key (ITEM_ID),
    constraint "comments_ITEMS_ITEM_ID_fk"
        foreign key (ITEM_ID) references ITEMS ON DELETE CASCADE ,
    constraint "comments_USERS_USER_ID_fk"
        foreign key (AUTHOR_ID) references USERS ON DELETE CASCADE
);

create table if not exists BOOKINGS
(
    BOOKING_ID     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    START_DATE     TIMESTAMP not null,
    END_DATE       TIMESTAMP not null,
    BOOKER_ID      BIGINT    not null,
    ITEM_ID        BIGINT    not null,
    BOOKING_STATUS CHARACTER VARYING(8),
    constraint BOOKINGS_PK
        primary key (BOOKING_ID),
    constraint "bookings_ITEMS_ITEM_ID_fk"
        foreign key (ITEM_ID) references ITEMS ON DELETE CASCADE ,
    constraint "bookings_USERS_USER_ID_fk"
        foreign key (BOOKER_ID) references USERS ON DELETE CASCADE ,
    constraint CHECK_DATE
        check (START_DATE < BOOKINGS.END_DATE)
);

