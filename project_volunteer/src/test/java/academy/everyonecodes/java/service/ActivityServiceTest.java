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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ActivityServiceTest {

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

    @Mock
    private Authentication auth;

    @BeforeEach
    public void initSecurityContext() {
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
    void postActivity_valid(){
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(userRepository.findByUsername(organizer.getUsername())).thenReturn(Optional.of(organizer));
        Mockito.when(activityRepository.save(activity)).thenReturn(activity);
        activityService.postActivity(activity);
        Mockito.verify(userRepository).findByUsername(organizer.getUsername());
        Mockito.verify(activityRepository).save(activity);
    }

    @Test
    void postDraft_valid() {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
        Mockito.when(draftRepository.save(draft)).thenReturn(draft);
        activityService.postDraft(draft);
        Mockito.verify(draftRepository).save(draft);
    }

    @Test
    void getDraftsOfOrganizer_valid() {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(organizer.getUsername());
      Mockito.when(draftRepository.findByOrganizer(organizer.getUsername())).thenReturn(List.of(draft));
      activityService.getDraftsOfOrganizer();
      Mockito.verify(draftRepository).findByOrganizer(organizer.getUsername());
    }

    /* GEHT NOCH NICHT:
    @Test
    void editDraft_valid() {
        Mockito.when(draftRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        //Mockito.when(activityService.postDraft(draft)).thenReturn(Optional.of(draft));
        Optional<Draft> oDraft = activityService.editDraft(draft);
        Assertions.assertEquals(Optional.of(draft), oDraft);
        Mockito.verify(draftRepository).findById(draft.getId());
    }

     */
}
