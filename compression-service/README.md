# Pinch - Compression Service

This is the media compression microservice for the Pinch project. It is a Spring Boot application that acts as a wrapper around the FFmpeg binary, providing a simple, production-ready REST API for compressing media files.

Currently, the service supports **Audio Compression** (converting to MP3 format).

---

## 🚀 Features (Phase 1)
- **Audio Compression API**: Upload an audio file (e.g., `.wav`, `.m4a`) and compress it to `.mp3`.
- **Configurable Bitrate**: Specify the target bitrate (default is `128k`).
- **External FFmpeg Execution**: Uses Java's `ProcessBuilder` to run FFmpeg natively for maximum performance.
- **Robust Error Handling**: Standardized JSON error responses and safe temporary file cleanup.
- **Docker Ready**: Fully containerized setup with FFmpeg baked into the runtime image.

---

## 🛠️ Tech Stack
- **Java 21 (LTS)**
- **Spring Boot 3.x**
- **Gradle**
- **FFmpeg** (External Binary)
- **Docker & Docker Compose**

---

## 📦 Running the Application

There are two ways to run the compression service:

### Option 1: Using Docker (Recommended)
This is the easiest way to run the service because the Docker image automatically installs the required FFmpeg binary and Java runtime. You do not need to install anything on your host machine other than Docker.

1. Ensure [Docker Desktop](https://www.docker.com/products/docker-desktop/) is running.
2. Open a terminal in the project root directory.
3. Build and start the container:
   ```bash
   docker compose up --build
   ```
4. The service will be available at `http://localhost:8080`.

*(Note: A local `./temp-data` folder will be created and mapped to the container's `/tmp/compression-service` directory so you can inspect temporary files during development.)*

### Option 2: Running Locally (Requires FFmpeg)
If you want to run the Spring Boot application directly on your machine, you must install FFmpeg manually.

1. **Install FFmpeg**:
   - Download and install FFmpeg for your OS (Windows, macOS, or Linux).
   - Note the path to the `ffmpeg` executable (e.g., `C:/ffmpeg/bin/ffmpeg.exe` or `/usr/local/bin/ffmpeg`).
2. **Configure Path**:
   - Open `src/main/resources/application.yml`.
   - Update the `compression.ffmpeg.path` property to match your local installation path.
3. **Build and Run**:
   - Open a terminal in the project root.
   - Run the application using the Gradle wrapper:
     ```bash
     ./gradlew bootRun
     ```
   - The service will be available at `http://localhost:8080`.

---

## 📡 API Documentation

### 1. Compress Audio
Upload an audio file to compress it to MP3 format.

**Endpoint:** `POST /api/v1/compress/audio`

**Content-Type:** `multipart/form-data`

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :---: | :--- |
| `file` | File (Multipart) | Yes | - | The source audio file to compress. |
| `bitrate` | String | No | `128k` | Target audio bitrate (e.g., `64k`, `128k`, `192k`, `320k`). |

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/compress/audio \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/audio.wav" \
  -F "bitrate=128k" --output compressed_audio.mp3
```

**Responses:**
- `200 OK`: Returns the compressed `.mp3` file stream as an attachment.
- `400 Bad Request`: If the uploaded file is empty or invalid.
- `413 Payload Too Large`: If the file exceeds the 500MB size limit.
- `500 Internal Server Error`: If FFmpeg processing fails or a file I/O error occurs.

---

## 🏗️ Project Structure Architecture
The project strictly follows a clean architecture:
- `controller/`: REST API endpoints. No business logic.
- `service/`: Core business logic and orchestration.
- `util/`: Low-level OS interactions (FFmpeg execution, File System operations).
- `config/`: Type-safe configuration properties.
- `exception/`: Global error handling and custom exceptions.
- `model/`: Data transfer objects and domain models.

---

## 🔮 Future Roadmap (Phase 2 & 3)
- **Video Compression**: Implementing H.264/libx264 endpoints with configurable CRF (quality) presets.
- **Asynchronous Processing**: Moving from synchronous HTTP requests to Job IDs and status polling endpoints.
- **Queue Integration**: Future readiness for Kafka/RabbitMQ.
- **Cloud Storage**: Seamless transition from local file storage to S3.
