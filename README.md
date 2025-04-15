## 개요

이 프로젝트는 Spring Boot를 기반으로 하며, 데이터베이스는 PostgreSQL을 사용합니다. 빌드 도구는 Gradle이며, pre-commit hook으로 코드 커밋 전에 자동으로 Spotless 코드 포맷터를 실행합니다.  
[출처: Spring Boot 공식 문서 - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

## 환경 요구사항

- **Java:** JDK 11 이상
- **빌드 도구:** Gradle (또는 Gradle Wrapper)
- **데이터베이스:** PostgreSQL 13 이상
- **IDE:** Vscode, IntelliJ IDEA, Eclipse 등 (선택 사항)

## 데이터베이스 설정

1.  **PostgreSQL 설치 및 실행**  
    PostgreSQL 공식 문서를 참고하여 설치 후 서버를 실행합니다.  
    [출처: PostgreSQL 공식 문서](https://www.postgresql.org/docs/)
2.  **데이터베이스 및 사용자 생성**  
    위의 운영체제별 명령어에 따라, 터미널(Git Bash, macOS/Linux) 또는 PowerShell(Windows)에서 다음 명령어를 실행하여 데이터베이스와 사용자를 생성합니다.

    ```
    createdb sampledb
    createuser -P your_username
    ```

    _your_username_ 및 비밀번호는 환경에 맞게 설정합니다.

3.  **application.properties 구성**  
    `src/main/resources/application.properties` 파일에 아래와 같이 PostgreSQL 접속 정보를 추가합니다.  
    application.properties 구성하실때 직접 만드시지말고 저한테 연락 주세요. 제가 파일 드릴게요.
    DB 부분만 만드신 db이름, username 및 pw로 바꾸시면됩니다.
    ```
    spring.datasource.url=jdbc:postgresql://localhost:5432/sampledb
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    spring.datasource.driver-class-name=org.postgresql.Driver
    ```

## 명령어 및 설정

### Windows

- **Pre-commit Hook 설정:**  
  Windows 환경에서는 Git Bash를 사용하는 것이 편리합니다.

  1.  프로젝트 루트의 `.git\hooks` 폴더로 이동합니다. (안보인다면 git을 설치후 해당 프로젝트를 clone 해주세요)
  2.  `pre-commit` 파일을 생성하고 아래 스크립트를 복사하여 붙여넣습니다.

      ```
      #!/bin/sh

      echo "🔧 Spotless 자동 포맷 실행 중..."
      ./gradlew spotlessApply

      if [ $? -ne 0 ]; then
        echo "❌ 포맷 도중 에러 발생. 커밋 중단됨."
        exit 1
      fi

      # 변경된 파일이 있는지 확인
      if ! git diff --quiet; then
      echo "⚠️ 포맷 결과로 변경사항이 생겼습니다."
      echo "➡️ git add . 후 다시 커밋하세요."
      exit 1
      fi

      echo "✅ 포맷 필요 없음. 커밋 진행됩니다."
      exit 0

      ```

  3.  Git Bash에서 아래 명령어로 실행 권한을 부여합니다.

      ```
      chmod +x .git/hooks/pre-commit
      ```

  만약 PowerShell이나 CMD를 사용한다면, 텍스트 에디터로 생성 후 Git Bash를 통해 권한 변경을 진행하거나, Git for Windows가 제공하는 도구를 활용합니다.

* **Gradle 명령어:**
  - 빌드:
    ```bat
    .\gradlew build
    ```
  - 애플리케이션 실행:
    ```bat
    .\gradlew bootRun
    ```
  - 테스트 실행:
    ```bat
    .\gradlew test
    ```
