
CREATE TABLE public.problems (
                                 id SERIAL PRIMARY KEY,
                                 title text NOT NULL,
                                 slug text NOT NULL,
                                 difficulty character varying(10),
                                 description text NOT NULL,
                                 solution jsonb,
                                 created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT problems_difficulty_check
                                     CHECK (difficulty IS NULL OR difficulty IN ('Easy', 'Medium', 'Hard'))
);


CREATE TABLE public.constraints (
                                    id integer NOT NULL,
                                    problem_id integer NOT NULL,
                                    constraint_text text NOT NULL
);

CREATE TABLE public.examples (
                                 id integer NOT NULL,
                                 problem_id integer NOT NULL,
                                 input text NOT NULL,
                                 output text NOT NULL,
                                 explanation text
);

CREATE TABLE public.problem_tags (
                                     problem_id integer NOT NULL,
                                     tag_id integer NOT NULL
);
CREATE TABLE public.tags (
                             id integer NOT NULL,
                             name character varying(100) NOT NULL
);

CREATE TABLE public.users (
                              id serial PRIMARY KEY,
                              firebase_uid character varying(255) NOT NULL UNIQUE,
                              username character varying(100) NOT NULL UNIQUE,
                              email character varying(255) NOT NULL UNIQUE,
                              created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              profile_url character varying(500)
);

CREATE TABLE public.user_leetcode_profiles (
                                               id integer NOT NULL,
                                               user_id integer NOT NULL,
                                               leetcode_username character varying(255) NOT NULL,
                                               total_solved integer DEFAULT 0,
                                               easy_solved integer DEFAULT 0,
                                               medium_solved integer DEFAULT 0,
                                               hard_solved integer DEFAULT 0,
                                               submission_calendar jsonb,
                                               last_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.user_problem_status (
                                            user_id integer NOT NULL,
                                            problem_id integer NOT NULL,
                                            is_solved boolean DEFAULT false,
                                            is_favorited boolean DEFAULT false
);

CREATE TABLE public.submissions (
                                    id SERIAL PRIMARY KEY,
                                    user_id INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
                                    problem_id INTEGER NOT NULL REFERENCES public.problems(id) ON DELETE CASCADE,
                                    solution_text TEXT NOT NULL,  -- user pseudocode input
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.evaluations (
                                    id SERIAL PRIMARY KEY,
                                    submission_id INTEGER NOT NULL REFERENCES public.submissions(id) ON DELETE CASCADE,
                                    version SMALLINT NOT NULL CHECK (version BETWEEN 1 AND 3), -- v1, v2, v3
                                    evaluation JSONB,  -- store structured evaluation: {rating, suggestions, corrections, feedback}
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE (submission_id, version)
);


CREATE TABLE public.learning_topics (
                                        id SERIAL PRIMARY KEY,
                                        topic_name VARCHAR(150) NOT NULL,
                                        content TEXT NOT NULL
);

CREATE TABLE public.problem_patterns (
                                         id SERIAL PRIMARY KEY,
                                         pattern_name VARCHAR(150) NOT NULL,
                                         content TEXT NOT NULL
);

CREATE TABLE public.problem_pattern_problems (
                                                 pattern_id INT NOT NULL REFERENCES public.problem_patterns(id) ON DELETE CASCADE,
                                                 problem_id INT NOT NULL REFERENCES public.problems(id) ON DELETE CASCADE,
                                                 PRIMARY KEY (pattern_id, problem_id)
);