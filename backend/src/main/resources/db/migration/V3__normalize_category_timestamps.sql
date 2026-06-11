UPDATE categories
SET created_at = replace(created_at, 'T', ' '),
    updated_at = replace(updated_at, 'T', ' ')
WHERE created_at LIKE '%T%' OR updated_at LIKE '%T%';
