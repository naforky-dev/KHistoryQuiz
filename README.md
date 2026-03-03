# KHistoryQuiz
마인크래프트에서 특정 시간마다 모든 플레이어에게 한국사 문제를 표시하는 플러그인

[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

>[!IMPORTANT]
> 이 플러그인을 유튜브와 같은 영상 플랫폼에 사용할 경우 플러그인 링크와 다음의 출처를 남겨주세요.
>
> 아이디어: `나포키`
>
> 제작자: `naforky-dev`


### License
이 플러그인은 GNU의 GPL-3.0 라이선스 하에 배포됩니다.

### 실행 환경
PaperMC `1.21.11`

Java 25 `temurin`(Oracle의 [최신 Java](https://www.oracle.com/java/technologies/downloads/) 버전)

### Usage
`/khistoryquiz <command>`

`<command>`:
  - `start` - 문제 디스플레이 타이머를 시작
  - `pause` - 타이머 일시정지
  - `stop`, `reset` - 타이머 정지, 문제 카운터와 점수, 죽은 횟수 등 리셋
  - `loaddefaults` - `config.yml`을 무시하고 기본 설정을 불러오기 (`questions.yml`은 일반적으로 로드됩니다)

`config.yml` - `/plugins/KHistoryQuiz` 경로에 생성 가능한 설정 파일(서버 새로고침, 재시작 시 설정 리셋 방지)

`questions.yml` - `/plugins/KHistoryQuiz` 경로에 생성 가능한 문제 세트 파일(기본 문제 세트 외 다른 문제 추가)

### 설정 파일
>[!NOTE]
>`config.yml` 파일을 아래의 방법 또는 구조와 다르게 생성할 경우 기본값이 로드됩니다.
>서버 로그에서 설정이 적용되었는지 확인할 수 있습니다.


```yml
# KHistoryQuiz 플러그인 설정
# question-set-filter: questions.yml의 문제들 중 특정 범위 또는 특정 문제들을 제외합니다.
# 예시: (문제들의 번호를 사용, 필터링을 하지 않는 경우 값을 none으로 입력)
question-set-filter: [1, 6, 9]


# interval: 게임 내에서 플러그인을 시작한 경우 문제가 나오는 시간 간격을 변경
# 'random' 또는 ms 단위의 시간 단위로 입력, random일 경우 플러그인이 4분(240000)마다 문제를 표시합니다.
# 1초 = 1000ms(밀리초)
interval: 240000
```

### 기본 옵션
>[!NOTE]
>아래의 설정은 위의 `config.yml` 파일이 없거나 잘못 작성된 경우
>플러그인이 기본적으로 로드하는 설정입니다.
```yml
question-set-filter: none
interval: 240000
```

---
> (c) 2026 [나포키(naforky)](https://youtube.com/@나포키), [naforky-dev](https://github.com/naforky-dev). All rights reserved.
