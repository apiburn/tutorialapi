
CREATE TABLE IF NOT EXISTS todo_lists (
    user_id  TEXT NOT NULL,
    id       TEXT NOT NULL,
    name     TEXT NOT NULL,

    CONSTRAINT todo_lists_pk PRIMARY KEY (user_id, id)
);

CREATE INDEX todo_lists_user_id_idx ON todo_lists (user_id);
CREATE INDEX todo_lists_id_idx ON todo_lists (id);


CREATE TABLE IF NOT EXISTS todo_items (
    user_id  TEXT NOT NULL,
    list_id  TEXT NOT NULL,
    id       TEXT NOT NULL,
    task     TEXT NOT NULL,
    done     BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT todo_items_pk PRIMARY KEY (user_id, list_id, id),
    CONSTRAINT todo_items_fk_user_id_list_id FOREIGN KEY (user_id, list_id)
        REFERENCES todo_lists (user_id, id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX todo_items_user_id_idx ON todo_items (user_id);
CREATE INDEX todo_items_list_id_idx ON todo_items (list_id);
CREATE INDEX todo_items_id_idx ON todo_items (id);
