package com.nhsd.a2si.capacityservice.persistence;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This test is set to be ignored during Maven builds etc. because the Redis tests presume a locally running
 * Redis instance is available and this will not normally be the case
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test-capacity-service-local-redis")
@Ignore
public class CapacityServiceApplicationRedisRepositoryTests {

    private static final String defaultServiceId = "defaultServiceId";

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    CapacityInformationRepositoryRedisImpl capacityInformationRepository;

	@Test
	public void testSaveCapacityInformation() {

	    String serviceId = "serviceId";



        capacityInformationRepository.deleteCapacityInformation(serviceId);

		CapacityInformation capacityInformation = new CapacityInformation();
		capacityInformation.setServiceId(serviceId);
        capacityInformation.setLastUpdated(dateTimeFormatter.format(LocalDateTime.now()));

		capacityInformationRepository.saveCapacityInformation(capacityInformation);
	}

    @Test
    public void testGetCapacityInformation() {

	    String lastUpdatedString = dateTimeFormatter.format(LocalDateTime.now());

        CapacityInformation capacityInformation = new CapacityInformation();
        capacityInformation.setServiceId(defaultServiceId);
        capacityInformation.setLastUpdated(lastUpdatedString);

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = capacityInformationRepository.getCapacityInformationByServiceId(defaultServiceId);
        assertEquals(capacityInformation.getServiceId(), defaultServiceId);
        assertEquals(capacityInformation.getMessage(), CapacityInformation.messageTemplate);
        assertEquals(capacityInformation.getLastUpdated(), lastUpdatedString);
    }

    @Test
    public void testDeleteCapacityInformationForServiceId() {

        LocalDateTime inOneMonth = LocalDateTime.now().plusMonths(1);

        CapacityInformation capacityInformation;

        String serviceId = "serviceId";

        capacityInformationRepository.deleteCapacityInformation(serviceId);

        capacityInformation = new CapacityInformation(serviceId, dateTimeFormatter.format(inOneMonth));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId, dateTimeFormatter.format(inOneMonth.plusDays(1)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId, dateTimeFormatter.format(inOneMonth.plusDays(2)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);


        capacityInformationRepository.deleteCapacityInformation(serviceId);

        capacityInformation = capacityInformationRepository.getCapacityInformationByServiceId(serviceId);
        assertNull(capacityInformation);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeleteAll() {

        LocalDateTime inOneMonth = LocalDateTime.now().plusMonths(1);

        CapacityInformation capacityInformation;

        String serviceId0001 = "serviceId0001";
        String serviceId0002 = "serviceId0002";

        capacityInformationRepository.deleteCapacityInformation(serviceId0001);
        capacityInformationRepository.deleteCapacityInformation(serviceId0002);

        capacityInformation = new CapacityInformation(serviceId0001, dateTimeFormatter.format(inOneMonth));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId0001, dateTimeFormatter.format(inOneMonth.plusDays(1)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId0001, dateTimeFormatter.format(inOneMonth.plusDays(2)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);



        capacityInformation = new CapacityInformation(serviceId0002, dateTimeFormatter.format(inOneMonth));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId0002, dateTimeFormatter.format(inOneMonth.plusDays(1)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        capacityInformation = new CapacityInformation(serviceId0002, dateTimeFormatter.format(inOneMonth.plusDays(2)));

        capacityInformationRepository.saveCapacityInformation(capacityInformation);


        capacityInformationRepository.deleteAll();

    }

}
