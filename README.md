# 작심백일 100DAYS
![100Image](https://github.com/YJ9352/100days/raw/dev/img/00.png)
## 프로젝트 개요
- 개발인원 : BE 3명
- 개발기간 : 24.02.26 ~ 24.04.05
- 프로젝트 설명 : Spring Boot를 이용한 100일 동기부여 목적달성 사이트 개발

## 개발환경
- Backend : Kotlin, Spring Boot, Spring Batch, Spring Data JPA, Spring Security, JWT, swagger 
- DB : Supabase (PostgreSQL), Amazon RDS, Redis
- Frontend : HTML5, CSS3, Vue.js
- Infra & CI/CD : EC2, S3, GitHub Actions
- Collaboration : IntelliJ IDEA, Git, GitHub Issues, Discord, Notion, Slack

## 프로젝트 소개
작심삼일이란 사자성어와 삼개월간 꾸준히 무언가를 하면 습관이 된다는 연구결과를 합쳐, 이용자가 100일간 이루어나갈 자신의 목표를 정하고 다른 유저와 함께 달리며 동기를 유지하기 위해 만들어진 프로젝트입니다.

## 소개 영상
<iframe width="560" height="315" src="https://www.youtube.com/embed/XkGbiIXkt78" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe

## 주요 개발 기능
### 회원 CRUD / 인증 & 인가
1. Spring Security를 이용한 인증&인가 프로세스를 구현하고 Access Token을 사용해 보안성 향상
2. 탈퇴 시 상태를 '탈퇴'로 변경하고 업데이트 날짜 기준으로 7일 뒤 Scheduled Batch로 일괄 삭제

   a. '탈퇴' 상태의 회원이 7일이내 재로그인 시 회원정보를 다시 정상 유저로 복구

   b. 위 과정을 통해 개인정보 보호 및 사용자 실수로 인한 데이터 소실 위험 개선
    ```kotlin
    // UserRepository
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.status = :status AND u.updatedAt <= :updatedAt")
    fun deleteUsersByStatusAndUpdatedAtIsLessThanEqualBatch(status: Status, updatedAt: LocalDateTime): Int
    
    // UserServiceImpl
    @Scheduled(cron = "0 0 12 * * ?")
    fun userDeletedAuto() {
        val nowTime = LocalDateTime.now()
        val userDeleteAuto = nowTime.minusDays(7)
        userRepository.deleteUsersByStatusAndUpdatedAtIsLessThanEqualBatch(Status.WITHDRAW, userDeleteAuto)
    }
    ```
4. 이메일을 분실했을 때 QueryDSL을 사용하여 닉네임을 이용한 검색기능을 제공

   a. 검색 결과는 앞에서 3자리 외 문자를 도메인 제외 마스킹 처리, 가입날짜를 yyyy-MM-dd로 출력되게 EmailResponse 내부에서 작성
5. 메세지 발송을 이용할 때 QueryDSL을 사용하여 회원 검색기능을 제공

### 메일 발송
1. JavaMailSender를 사용하여 가입을 위한 이메일 인증번호 발송 기능을 추가

   a. 기존 배치 사용시 인증코드를 반복해서 발급할 경우 속도가 눈에 띄게 느려지는 문제가 발생 DB에서 Redis로 전환하여 문제를 해결하였고 코드 복잡성 감소 및 응답 속도 20% 향상

   b. 최근 발급된 인증 코드만 비교해 성공 여부와 상관없이 5분 후 자동 삭제하여 보안성 강화
    ```kotlin
       @Component
    class RedisUtil(
        private val stringRedisTemplate: StringRedisTemplate,
        private val redisTemplate: RedisTemplate<String, String>
    ) {
        // 메일 인증코드 확인
        fun getDataMatch(key: String, value: String, duration: Long): String? {
            val storedValue = getData(key)
            val oldValue = stringRedisTemplate.opsForValue().getAndSet(key, value)
            if (oldValue == storedValue) {
                Duration.ofSeconds(duration)
                setDataExpire(key, value, duration)
                return oldValue
            } else {
                return null
            }
        }
     }
     
    @Service
    class MailServiceImpl(
        val mailUtility: MailUtility,
        val redisUtil: RedisUtil
    ) : MailService {
    
        // 인증메일 발송
        override fun sendVerificationEmail(request: EmailRequest) {
            mailUtility.emailSender(request.email, MailType.VERIFYCODE)
        }
    
        // 코드 일치 확인
        override fun verifyCode(code: String, email: String): String {
            return redisUtil.getDataMatch(code, email, 300) ?: throw AuthCodeMismatchException()
        }
    }
    ```
3. 비밀번호 찾기는 임시 비밀번호를 이미 가입 된 사용자의 메일 주소에 재발급하는 형태로 구현하여 보안성 강화 및 편의성을 향상
4. 반복되는 로직을 Component화 하여 확장성을 강화하고 용도에 따라 Enum 타입을 활용하여 구분함으로서 기존에 비해 코드량을 40% 감소시키는데 성공
    ```kotlin
    @Component
    class MailUtility(
        private val passwordEncoder: PasswordEncoder,
        private val regexFunc: RegexFunc,
        private val randomCode: RandomCode,
        private val redisUtil: RedisUtil,
        @Value("\${mail.username}") private val username: String,
        @Autowired val javaMailSender: JavaMailSender
    ) {
    
        // 메일전송
        fun emailSender(email: String, type: MailType): String {
            val code = randomCode.generateRandomCode(10) // 10자리 랜덤문자 생성
            val pass = passwordEncoder.encode(regexFunc.regexPassword(code)) // 암호화
    
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
    
            helper.setTo(email)
    
            if (type == MailType.VERIFYCODE) {
                // 회원가입용 인증번호 요청일 때
                val redisKey = "verification:$email" // 메일 주소를 기반으로 레디스 키 생성
                redisUtil.setDataExpire(code, redisKey, 300) // redis에 email과 code저장, 인증시간 5분
                helper.setSubject("회원가입을 위한 이메일 인증번호입니다.")
                helper.setText("이메일 인증 번호는 $code 입니다.")
                helper.setFrom(username)
                javaMailSender.send(message)
                
                return code
            } else if (type == MailType.CHANGEPASSWORD) {
                // 비밀번호 재발급일 때
                helper.setSubject("임시 비밀번호를 발급해드립니다.")
                helper.setText(
                    "임시 비밀번호는 $code 입니다. \n " +
                            "로그인 하신 뒤, 반드시 비밀번호를 변경해주세요."
                )
                helper.setFrom(username)
                javaMailSender.send(message)
    
                return pass
    
            } else {
                throw ModelNotFoundException("email")
            }
        }
    }
    ```
5. 임시 비밀번호 발급, 가입인증 등에 사용되는 랜덤 문자열 발급기능을 분리 및 개선

   a. 중복코드를 줄이고 범용성있게 사용하기 위해 콤포넌트 형태로 분리 후 리팩토링

   b. regexFunc를 통해 유효성 검증을 구현하여 보안을 강화하고 다양한 사용 목적을 충족

### 포스트 / 코멘트 CRUD
1. 단문 및 장문 포스트를 선택하여 입력할 수 있도록 선택지를 제공하여 편의성 향상
2. 수정 시 콘텐츠 타입 변경 가능하며 작성 완료 후 목표 달성율에 자동 반영되어 사용자 경험 증대
3. 포스트 조회 화면 내부에 코멘트 기능을 추가

### 프론트엔드
1. 디자인 및 와이어프레임을 직접 작성하여 팀 전체의 작업속도를 단축
2. Vue.js의 API Client를 통해 백엔드와의 데이터 송수신 기능을 구축 및 관리
   
### 기타
Swagger를 활용하여 RESTful API를 명확하게 정의하고 문서화 함

## 트러블 슈팅
### 메일 발송 기능 추가 중 JavaMailSender 연동 실패로 인한 오류 발생
![javamailsender.png](https://github.com/YJ9352/100days/raw/dev/img/javamailsender.png)

MailConfig 클래스에 JavaMailSenderImpl의 Bean을 정의하고, MailUtility에는 @Autowired를 사용하여 의존성 주입을 통해 문제 해결

### Scheduled를 이용한 회원 탈퇴가 제대로 이루어지지 않아 문제 발생
![scheduled.png](https://github.com/YJ9352/100days/raw/dev/img/scheduled.png)

isDelete 컬럼명이 예약어와 겹쳤던것이 원인이므로, 회원 Status를 관리하던 컬럼의 WITHDRAW를 사용해서 해결

## 아키텍처
![architecture.png](https://github.com/YJ9352/100days/raw/dev/img/architecture.png)

## 와이어프레임
![wire-frame.png](https://github.com/YJ9352/100days/raw/dev/img/wire-frame.png)

## ERD
![erd.png](https://github.com/YJ9352/100days/raw/dev/img/erd.png)

## API 명세서
![API.png](https://github.com/YJ9352/100days/raw/dev/img/API.png)



