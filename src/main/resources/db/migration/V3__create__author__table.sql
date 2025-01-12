create table author (
    id          serial primary key,
    fullname    text not null,
    create_at   timestamp default current_timestamp not null
);

alter table budget add column author_id int references author(id);