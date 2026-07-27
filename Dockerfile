# Use Eclipse Temurin JRE 25 (modern, actively maintained OpenJDK LTS distribution)
FROM eclipse-temurin:25-jre

# Set working directory
WORKDIR /app

# pg_dump is required for the in-app database backup feature (DbBackupService)
RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user (docker:S6471) instead of the image's default root user.
# UID/GID default to 1000 (common Synology non-privileged default) and can be
# overridden at build time, e.g.:
#   docker build --build-arg APP_UID=1026 --build-arg APP_GID=100 .
# IMPORTANT: the host directories bind-mounted at runtime (media/backup volumes
# in docker-compose.yml) must be writable by this UID/GID on the NAS, otherwise
# media uploads and backups will fail with permission errors.
ARG APP_UID=1000
ARG APP_GID=1000
RUN (getent group ${APP_GID} || groupadd -g ${APP_GID} appgroup) \
    && (getent passwd ${APP_UID} || useradd -u ${APP_UID} -g ${APP_GID} -M -s /usr/sbin/nologin appuser) \
    && chown -R ${APP_UID}:${APP_GID} /app

# Copy the built JAR from the previous stage
COPY --chown=${APP_UID}:${APP_GID} ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar

# Expose application port
EXPOSE 8080

# Health check (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -version || exit 1

USER ${APP_UID}:${APP_GID}

# Run the application
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]
