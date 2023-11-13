CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    rating double precision NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    lat double precision NOT NULL,
    lon double precision NOT NULL
    );

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    annotation VARCHAR NOT NULL,
    category_id BIGINT NOT NULL,
    confirmed_requests BIGINT NOT NULL,
    created_on TIMESTAMP(9) NOT NULL,
    description VARCHAR NOT NULL,
    event_date TIMESTAMP(9) NOT NULL,
    initiator_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit BIGINT NOT NULL,
    published_on TIMESTAMP(9),
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(64) NOT NULL,
    title VARCHAR NOT NULL,
    rating double precision NOT NULL,
    CONSTRAINT fk_category_id  FOREIGN KEY(category_id) REFERENCES categories(id),
    CONSTRAINT fk_initiator_id  FOREIGN KEY(initiator_id) REFERENCES users(id),
    CONSTRAINT fk_location_id  FOREIGN KEY(location_id) REFERENCES locations(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_compilation (
     event_id BIGINT NOT NULL,
     compilation_id BIGINT NOT NULL,
     CONSTRAINT pk_event_compilation PRIMARY KEY (event_id, compilation_id),
     CONSTRAINT fk_event_id FOREIGN KEY(event_id) REFERENCES events(id),
     CONSTRAINT fk_compilation_id FOREIGN KEY(compilation_id) REFERENCES compilations(id)
    );

CREATE TABLE IF NOT EXISTS requests (
     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
     created TIMESTAMP(9) NOT NULL,
     event_id BIGINT NOT NULL,
     requester_id BIGINT NOT NULL,
     status VARCHAR(10) NOT NULL,
     CONSTRAINT fk_event_request  FOREIGN KEY(event_id) REFERENCES events(id),
     CONSTRAINT fk_requester_id  FOREIGN KEY(requester_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS rating (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    event_id BIGINT NOT NULL,
    estimator_id BIGINT NOT NULL,
    mark BOOLEAN NOT NULL,
    CONSTRAINT fk_event_rating  FOREIGN KEY(event_id) REFERENCES events(id),
    CONSTRAINT fk_estimator_id FOREIGN KEY(estimator_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS visiting (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    event_id BIGINT NOT NULL,
    visitor_id BIGINT NOT NULL,
    created_on TIMESTAMP(9) NOT NULL,
    CONSTRAINT fk_event_visiting  FOREIGN KEY(event_id) REFERENCES events(id),
    CONSTRAINT fk_visitor_id FOREIGN KEY(visitor_id) REFERENCES users(id)
    );
