package com.bravostudiodev.grid.session;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.grid.internal.ExternalSessionKey;
import org.openqa.grid.internal.DefaultGridRegistry;
import org.openqa.grid.internal.TestSession;

import java.time.Clock;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumSessionsTest {

    @Mock
    private DefaultGridRegistry registry;
    @Mock
    private ExternalSessionKey externalSessionKey;

    private TestSession activeSession;

    private SeleniumSessions seleniumSessions;

    @Before
    public void setUp() {
        seleniumSessions = new SeleniumSessions(registry);
        activeSession = spy(new TestSession(null, null, Clock.systemUTC()));

        when(activeSession.getExternalKey()).thenReturn(externalSessionKey);
        when(externalSessionKey.getKey()).thenReturn("sessionId");
        when(registry.getActiveSessions()).thenReturn(Sets.newHashSet(activeSession));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSessionIdExceptional() {
        SeleniumSessions.getSessionIdFromPath("/sessionId/");
    }

    @Test
    public void getSessionIdFromPath() {
        assertEquals("sessionId", SeleniumSessions.getSessionIdFromPath("/session/sessionId/"));
        assertEquals("sessionId", SeleniumSessions.getSessionIdFromPath("/session/sessionId/getCurrentWindow"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void trimSessionPathExceptional() {
        SeleniumSessions.trimSessionPath("/sessionId/");
    }

    @Test
    public void trimSessionPath() {
        assertEquals("", SeleniumSessions.trimSessionPath("/session/sessionId"));
        assertEquals("/request", SeleniumSessions.trimSessionPath("/session/id/request"));
        assertEquals("/another/one", SeleniumSessions.trimSessionPath("/session/id/another/one"));
    }

    @Test
    public void shouldRefreshTimeout() throws InterruptedException {
        Thread.sleep(100);
        long inactivityTime = activeSession.getInactivityTime();
        Thread.sleep(100);

        seleniumSessions.refreshTimeout("sessionId");

        long inactivityTimeAfterRefresh = activeSession.getInactivityTime();

        assertTrue(String.format("Inactivity time should be less after refresh, but have %d > %d", inactivityTime, inactivityTimeAfterRefresh),
                inactivityTime > inactivityTimeAfterRefresh);
    }

    @Test
    public void shouldNotRefreshTimeoutIfTimeoutIsIgnored() {
        activeSession.setIgnoreTimeout(true);

        long inactivityTime = activeSession.getInactivityTime();

        seleniumSessions.refreshTimeout("sessionId");

        long inactivityTimeAfterRefresh = activeSession.getInactivityTime();

        assertTrue(inactivityTime == 0);
        assertTrue("Inactivity time should be 0 when timeout is ignored", inactivityTime == inactivityTimeAfterRefresh);
    }
}
