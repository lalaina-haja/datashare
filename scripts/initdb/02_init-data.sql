-- ============================================================
-- 1. Création de l'utilisateur
-- ============================================================

INSERT INTO users (email, password, created_at)
VALUES (
    'exemple@mail.fr',
    -- Mot de passe "Passw0rd!" encodé en bcrypt
    '$2a$12$kGP77UTXiN8FIHSWVrofmuN51ipvplYx0UIVekPUmRAOBjPCmX.JC',
    NOW()
);

-- ============================================================
-- 2. Insertion des données de démonstration
-- ============================================================

DO $$
DECLARE
    v_user_id BIGINT;
    v_file1 BIGINT;
    v_file2 BIGINT;
    v_file3 BIGINT;
    v_file4 BIGINT;
    v_file5 BIGINT;
BEGIN
    -- Récupération de l'utilisateur
    SELECT id INTO v_user_id FROM users WHERE email = 'exemple@mail.fr';

    -- Fichiers actifs
    INSERT INTO files (user_id, filename, content_type, size, s3_key, created_at)
    VALUES
        (v_user_id, 'rapport.pdf', 'application/pdf', 245678, 'files/rapport_2024.pdf', NOW()),
        (v_user_id, 'photo.png', 'image/png', 128900, 'files/photo_2024.png', NOW()),
        (v_user_id, 'notes.txt', 'text/plain', 3400, 'files/notes_2024.txt', NOW());

    SELECT id INTO v_file1 FROM files WHERE filename = 'rapport.pdf';
    SELECT id INTO v_file2 FROM files WHERE filename = 'photo.png';
    SELECT id INTO v_file3 FROM files WHERE filename = 'notes.txt';

    INSERT INTO tokens (token_string, file_id, expires_at)
    VALUES
        ('ACTIVETOKEN1', v_file1, NOW() + INTERVAL '24 hours'),
        ('ACTIVETOKEN2', v_file2, NOW() + INTERVAL '24 hours'),
        ('ACTIVETOKEN3', v_file3, NOW() + INTERVAL '24 hours');

    -- Fichiers expirés
    INSERT INTO files (user_id, filename, content_type, size, s3_key, created_at)
    VALUES
        (v_user_id, 'archive.zip', 'application/zip', 5600000, 'files/archive_2023.zip', NOW() - INTERVAL '10 days'),
        (v_user_id, 'old_doc.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            78000, 'files/old_doc_2023.docx', NOW() - INTERVAL '20 days');

    SELECT id INTO v_file4 FROM files WHERE filename = 'archive.zip';
    SELECT id INTO v_file5 FROM files WHERE filename = 'old_doc.docx';

    INSERT INTO tokens (token_string, file_id, expires_at)
    VALUES
        ('EXPIREDTOK1', v_file4, NOW() - INTERVAL '2 days'),
        ('EXPIREDTOK2', v_file5, NOW() - INTERVAL '5 days');
END $$;