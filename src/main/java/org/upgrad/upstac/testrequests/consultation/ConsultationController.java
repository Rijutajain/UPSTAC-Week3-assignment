package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);


    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;


    @Autowired
    TestRequestFlowService testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;


    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations() {
        //This method is implemented to get the list of test requests having
        // status as 'LAB_TEST_COMPLETED'.
        // Uses findBy() method from testRequestQueryService class
        //which returns the List of test requests whose status is
        // LAB_TEST_COMPLETED.
        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor() {
        // Creates an object of User class and stores the current logged-in
        // user which is a doctor in this case.
        //This method returns the list of test requests assigned
        // to current doctor(Using above created User object).
        //Uses findByDoctor() method from testRequestQueryService
        // class to get the list.
        User doctor = userLoggedInService.getLoggedInUser();
        return testRequestQueryService.findByDoctor(doctor);
    }


    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        // This method assigns a particular test request to the
        // current doctor(logged-in user).
        //Creates an object of User class and get the current logged-in user
        //Uses the assignForConsultation() method of testRequestUpdateService
        // to assign the test id to the current doctor.
        // return created object which is a test request.
        //In case of invalid test request id,throws an exception.
        try {
            User doctor = userLoggedInService.getLoggedInUser();
            return testRequestUpdateService.assignForConsultation(id, doctor);
        } catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id, @RequestBody CreateConsultationRequest testResult) {
        // This method updates the result of the current test
        // request id with doctor's comments.
        // Creates an object of the User class to get the logged-in user which
        // is a doctor in this case.
        // Uses updateConsultation() method from
        // testRequestUpdateService class which takes test Request id,
        // Consultation (given as parameter) and logged-in user.
        try {
            User doctor = userLoggedInService.getLoggedInUser();
            return testRequestUpdateService.updateConsultation(id, testResult,
                    doctor);
        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        } catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }


}
