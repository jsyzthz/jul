package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.openbase.jul.schedule.RecurrenceEventFilterTest.RecurrenceEventFilterImpl.TIMEOUT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class RecurrenceEventFilterTest {

    public RecurrenceEventFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of trigger method, of class RecurrenceEventFilter.
     * @throws java.lang.InterruptedException
     */
    @Test(timeout = 5000)
    public void testRecurrenceEventFilter() throws InterruptedException {
        System.out.println("trigger");
        RecurrenceEventFilterImpl instance = new RecurrenceEventFilterImpl();

        for (int i = 0; i < 100; i++) {
            instance.trigger();
        }
        Thread.sleep(TIMEOUT + 20);
        assertEquals(2, instance.getRelayCounter());
    }

    public class RecurrenceEventFilterImpl extends RecurrenceEventFilter {

        public static final long TIMEOUT = 50;

        private int relayCounter = 0;

        public RecurrenceEventFilterImpl() {
            super(TIMEOUT);
        }

        @Override
        public void relay() {
            relayCounter++;
        }

        public int getRelayCounter() {
            return relayCounter;
        }
    }
}