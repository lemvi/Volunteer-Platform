package academy.everyonecodes.java.service;

import academy.everyonecodes.java.data.DTOs.CompanyProfileDTO;
import academy.everyonecodes.java.data.DTOs.IndividualProfileDTO;
import academy.everyonecodes.java.data.DTOs.VolunteerProfileDTO;
import academy.everyonecodes.java.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserToProfileDTOTranslator
{
    private final AgeCalculator ageCalculator;
    @Autowired
    RatingCalculator ratingCalculator;
    private final SkillService skillService;


    public UserToProfileDTOTranslator(AgeCalculator ageCalculator, SkillService skillService)
    {
        this.ageCalculator = ageCalculator;
        this.skillService = skillService;
    }


    protected CompanyProfileDTO toCompanyProfileDTO(User user)
    {
        return new CompanyProfileDTO(
                user.getUsername(),
                user.getPostalCode(),
                user.getCity(),
                user.getStreet(),
                user.getStreetNumber(),
                user.getEmailAddress(),
                user.getTelephoneNumber(),
                user.getDescription(),
                user.getRoles(),
                ratingCalculator.aggregateRating(user.getId()),
                user.getCompanyName()
        );
    }

    protected IndividualProfileDTO toIndividualProfileDTO(User user)
    {
        return new IndividualProfileDTO(
                user.getUsername(),
                user.getPostalCode(),
                user.getCity(),
                user.getStreet(),
                user.getStreetNumber(),
                user.getEmailAddress(),
                user.getTelephoneNumber(),
                user.getDescription(),
                user.getRoles(),
                ratingCalculator.aggregateRating(user.getId()),
                user.getFirstNamePerson() + " " + user.getLastNamePerson(),
                ageCalculator.calculate(user)
        );
    }

    protected VolunteerProfileDTO toVolunteerProfileDTO(User user)
    {
        return new VolunteerProfileDTO(
                user.getUsername(),
                user.getPostalCode(),
                user.getCity(),
                user.getStreet(),
                user.getStreetNumber(),
                user.getEmailAddress(),
                user.getTelephoneNumber(),
                user.getDescription(),
                user.getRoles(),
                ratingCalculator.aggregateRating(user.getId()),
                user.getFirstNamePerson() + " " + user.getLastNamePerson(),
                ageCalculator.calculate(user),
                skillService.collect(user)
        );
    }
}
