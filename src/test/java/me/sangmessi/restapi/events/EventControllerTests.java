package me.sangmessi.restapi.events;

import me.sangmessi.restapi.accounts.Account;
import me.sangmessi.restapi.accounts.AccountRepository;
import me.sangmessi.restapi.accounts.AccountRole;
import me.sangmessi.restapi.accounts.AccountService;
import me.sangmessi.restapi.common.BaseControllerTest;
import me.sangmessi.restapi.commons.TestDescription;
import me.sangmessi.restapi.configs.AppProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp(){
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2018,12,17,22, 59))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 12, 20, 23, 59))
                .endEventDateTime(LocalDateTime.of(2018, 12, 21, 23, 59))
                .beginEnrollmentDateTime(LocalDateTime.of(2018,12,21,00,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();


        mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an exsiting event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("Start time of Enrollment"),
                                fieldWithPath("closeEnrollmentDateTime").description("Close time of Enrollment"),
                                fieldWithPath("beginEventDateTime").description("Start time of new event"),
                                fieldWithPath("endEventDateTime").description("End time of new event"),
                                fieldWithPath("location").description("Location of new event"),
                                fieldWithPath("basePrice").description("Base price of new event"),
                                fieldWithPath("maxPrice").description("Max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("The number of limitation of new event")

                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("Identification of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("Start time of Enrollment"),
                                fieldWithPath("closeEnrollmentDateTime").description("Close time of Enrollment"),
                                fieldWithPath("beginEventDateTime").description("Start time of new event"),
                                fieldWithPath("endEventDateTime").description("End time of new event"),
                                fieldWithPath("location").description("Location of new event"),
                                fieldWithPath("basePrice").description("Base price of new event"),
                                fieldWithPath("maxPrice").description("Max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("The number of limitation of new event"),
                                fieldWithPath("offline").description("It tells that this event is offline or online"),
                                fieldWithPath("free").description("It tells that this event is free or not"),
                                fieldWithPath("eventStatus").description("Status of new event"),
                                fieldWithPath("_links.self.href").description("Link to self"),
                                fieldWithPath("_links.query-events.href").description("Link to query events list"),
                                fieldWithPath("_links.update-event.href").description("Link to update existing event"),
                                fieldWithPath("_links.profile.href").description("Link to update existing event")

                        )
                        ))
        ;

    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_bad_request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2018,12,17,22, 59))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 12, 20, 23, 59))
                .endEventDateTime(LocalDateTime.of(2018, 12, 21, 23, 59))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();


        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())

        ;

    }

    @Test
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());

    }



    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,12,21,00,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 12, 20, 23, 59))
                .beginEventDateTime(LocalDateTime.of(2018,12,17,12, 59))
                .endEventDateTime(LocalDateTime.of(2018, 12, 21, 23, 59))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;

    }

    @Test
    @TestDescription("인증 정보와 함께 30개의 이벤트를 10개씩 두번 조회하기 ")
    public void queryEventsWithAuthorization() throws Exception {
        //Given
        IntStream.range(0, 30).forEach(this::generateEvents);

        //When & Then
        this.mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events"))
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번 조회하기 ")
    public void queryEvents() throws Exception {
        //Given
        IntStream.range(0, 30).forEach(this::generateEvents);

        //When & Then
        this.mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기 ")
    public void getEvent() throws Exception {
        // Given
        Event event = this.generateEvents(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
                ;
    }

    @Test
    @TestDescription("없는 이벤트를 하나 조회하기 ")
    public void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/123123123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("기존 이벤트를 정상적으로 수정하기 ")
    public void updateEvent() throws Exception {
        // Given
        Event event = this.generateEvents(100);
        String eventName = "Updated Event";
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event"))
                ;
    }

    @Test
    @TestDescription("입력값이 없는 경우에  이벤트 수정 실패")
    public void updateEvent400_empty() throws Exception {
        // Given
        Event event = this.generateEvents(100);
        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(this.objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에  이벤트 수정 실패")
    public void updateEvent400_wrong() throws Exception {
        // Given
        Event event = this.generateEvents(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(100000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        // Given
        Event event = this.generateEvents(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/123123")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }



    private Event generateEvents(int index) {
        Event event = Event.builder()
                .name("event "+index)
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2018,12,17,22, 59))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 12, 20, 23, 59))
                .endEventDateTime(LocalDateTime.of(2018, 12, 21, 23, 59))
                .beginEnrollmentDateTime(LocalDateTime.of(2018,12,21,00,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }

    private String getAccessToken() throws Exception {
        String clientId = appProperties.getClientId();
        String password = appProperties.getClientSecret();

        String username = appProperties.getAdminUsername();
        String userPassword = appProperties.getAdminPassword();
        Account admin = Account.builder()
                .email(username)
                .password(userPassword)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(admin);

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(clientId, password))
                .param("username", username)
                .param("password", userPassword)
                .param("grant_type", "password")
        );
        String content = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser jsonParser = new Jackson2JsonParser();
        return jsonParser.parseMap(content).get("access_token").toString();
    }

    private String getBearerToken() throws Exception{
        return "Bearer "+getAccessToken();
    }

}
