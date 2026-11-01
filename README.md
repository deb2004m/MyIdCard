# Gym ID Card API

A Spring Boot 3 REST API and browser form for registering gym ID cards. The root page (`/`) serves the existing ID-card design and its local assets.

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 8+

## Run locally

Create or use a MySQL account with permission to create the `gym_cards` database. The JDBC URL creates that database automatically when permitted. On Windows, set local configuration in `.env` and start the app with:

```powershell
.\run.ps1
```

The local `.env` file is ignored by Git and the launcher restricts its Windows file permissions to your account and SYSTEM. It contains plaintext values on disk; do not commit it, share it, or use it to store personal cardholder details. Copy `.env.example` if you need to recreate it. If `DB_PASSWORD` is blank, the launcher prompts for it without echoing the input. The MySQL service must be reachable on port 3306. Open `http://localhost:8080/index.html` so the page and API use the same origin, submit the registration form, and the two-sided card will appear. Use **Download card as PDF** to save the full-width, white-background browser layout (front and back together) as one PDF image. If you instead serve the page with VS Code Live Server on port 5500, the API is configured to accept that local origin; restart Spring Boot after changing backend code. Uploaded JPEG, PNG, and GIF images are validated and stored under `uploads/` using generated filenames. Set `UPLOAD_DIRECTORY` to change the storage location.

## API

- `POST /api/cards/register` — multipart fields `name`, `bloodGroup`, `phoneNumber`, `preferredHour` (`1`-`12`), `preferredMinute` (`00`-`59`), `preferredPeriod` (`AM` or `PM`), `validUntil` (`YYYY-MM`, optional for API clients; defaults to next month), and `profilePic`; returns the created card JSON with HTTP 201.
- `GET /api/cards/{gymId}` — returns a card by its generated gym ID.
- `GET /uploads/{filename}` — serves a stored profile picture.

The database connection can be configured with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Uploads are limited to 5 MB and image dimensions are bounded before decoding.
