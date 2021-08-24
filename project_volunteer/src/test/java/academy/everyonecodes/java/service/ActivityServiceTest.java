package academy.everyonecodes.java.service;

import academy.everyonecodes.java.data.Activity;
import academy.everyonecodes.java.data.Draft;
import academy.everyonecodes.java.data.Role;
import academy.everyonecodes.java.data.User;
import academy.everyonecodes.java.data.repositories.ActivityRepository;
import academy.everyonecodes.java.data.repositories.DraftRepository;
import academy.everyonecodes.java.data.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ActivityServiceTest
{

    @Autowired
    ActivityService activityService;

    @MockBean
    private ActivityRepository activityRepository;

    @MockBean
    private ActivityDraftTranslator translator;

    @MockBean
    private DraftRepository draftRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;


    private String endDateBeforeStartDate = "bad request";

    @Mock
    private Authentication auth;

    @BeforeEach
    public void initSecurityContext()
    {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    Set<Role> roles = new HashSet<>(List.of(new Role(2L, "ROLE_INDIVIDUAL")));
    User organizer = new User(
            "username",
            "password",
            "email@email.com",
            roles
    );
    Set<User> applicants = new HashSet<>();
    Set<User> participants = new HashSet<>();
    String categories = "oneCategory";
    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.of(2100, 1, 1), LocalTime.of(10, 10, 10));
    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.of(2100, 1, 1), LocalTime.of(10, 10, 10));

    Activity activity = new Activity(
            "title",
            "descr",
            "skills",
            List.of(categories),
            startDateTime,
            endDateTime,
            false,
            organizer,
            applicants,
            participants);

    Draft draft = new Draft(
            "title",
            "descr",
            "skills",
            categories,
            startDateTime,
            endDateTime,
            false,
            organizer.getUsername());

    @Test
    void postActivity_valid()
    {
        Mockito.when(translator.toActivity(draft)).thenReturn(activity);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(userRepository.findByUsername(organizer.getUsername())).thenReturn(Optional.of(organizer));
        Mockito.when(activityRepository.save(activity)).thenReturn(activity);

        Activity actual = activityService.postActivity(draft);
        Assertions.assertEquals(activity, actual);

        Mockito.verify(translator).toActivity(draft);
        Mockito.verify(draftRepository).delete(draft);
        Mockito.verify(userRepository).findByUsername(organizer.getUsername());
        Mockito.verify(activityRepository).save(activity);
    }

    @Test
    void postDraft_valid()
    {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(draftRepository.save(draft)).thenReturn(draft);

        Draft actual = activityService.postDraft(draft);
        Assertions.assertEquals(draft, actual);

        Mockito.verify(draftRepository).save(draft);
    }

    @Test
    void getDraftsOfOrganizer_valid()
    {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(draftRepository.findByOrganizerUsername(organizer.getUsername())).thenReturn(List.of(draft));

        List<Draft> actual = activityService.getDraftsOfOrganizer();
        Assertions.assertEquals(List.of(draft), actual);

        Mockito.verify(draftRepository).findByOrganizerUsername(organizer.getUsername());
    }


    @Test
    void editDraft_valid()
    {
        draft.setId(1L);
        Mockito.when(draftRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(draftRepository.save(draft)).thenReturn(draft);

        Draft actual = activityService.editDraft(draft);
        Assertions.assertEquals(draft, actual);
        Mockito.verify(draftRepository).findById(draft.getId());
        Mockito.verify(draftRepository).save(draft);

    }

    @Test
    void editDraft_isEmpty()
    {
        draft.setId(1L);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(draftRepository.findById(draft.getId())).thenReturn(Optional.empty());
        Draft actual = activityService.editDraft(draft);
        Mockito.verify(userService, times(1)).throwBadRequest("BAD_REQUEST: No matching draft was found.");
    }

    @Test
    void saveDraftAsActivity_valid()
    {
        draft.setId(1L);
        Mockito.when(draftRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        Mockito.when(translator.toActivity(draft)).thenReturn(activity);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(userRepository.findByUsername(organizer.getUsername())).thenReturn(Optional.of(organizer));
        Mockito.when(activityRepository.save(activity)).thenReturn(activity);


        Activity actual = activityService.saveDraftAsActivity(draft.getId());
        Assertions.assertEquals(activity, actual);

        Mockito.verify(translator).toActivity(draft);
        Mockito.verify(draftRepository).delete(draft);
        Mockito.verify(userRepository).findByUsername(organizer.getUsername());
        Mockito.verify(activityRepository).save(activity);

    }

    @Test
    void saveDraftAsActivity_isEmpty()
    {
        draft.setId(1L);
        Mockito.when(draftRepository.findById(draft.getId())).thenReturn(Optional.empty());
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(translator.toActivity(new Draft())).thenReturn(new Activity(true));
        Activity actual = activityService.saveDraftAsActivity(draft.getId());
        Mockito.verify(userService, times(1)).throwBadRequest("BAD_REQUEST: No matching draft was found.");
    }

    @Test
    void getActivitiesOfOrganizer_valid()
    {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(activityRepository.findByOrganizer_Username(organizer.getUsername())).thenReturn(List.of());

        List<Activity> actual = activityService.getActivitiesOfOrganizer("username");
        Assertions.assertEquals(List.of(), actual);

        Mockito.verify(activityRepository).findByOrganizer_Username(organizer.getUsername());
    }

    @Test
    void findById()
    {
        Mockito.when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        activityService.findActivityById(1L);

        verify(activityRepository).findById(1L);
    }

    @Test
    void findAll()
    {
        activityService.getAllActivities();

        verify(activityRepository).findAll();
    }

}
