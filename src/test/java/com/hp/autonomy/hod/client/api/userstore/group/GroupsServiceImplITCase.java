/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.hod.client.api.userstore.group;

import com.hp.autonomy.hod.client.AbstractHodClientIntegrationTest;
import com.hp.autonomy.hod.client.Endpoint;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.userstore.StatusResponse;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

// TODO: Add assign/remove user from group tests when it is possible to create a user, or at least to get an existing user's UUID
@RunWith(Parameterized.class)
public class GroupsServiceImplITCase extends AbstractHodClientIntegrationTest {
    private static final EnumSet<HodErrorCode> GROUP_OR_USER_NOT_FOUND = EnumSet.of(HodErrorCode.GROUP_NOT_FOUND, HodErrorCode.USER_NOT_FOUND);

    private GroupsService service;
    private List<String> createdGroups;

    public GroupsServiceImplITCase(final Endpoint endpoint) {
        super(endpoint);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        createdGroups = new LinkedList<>();
        service = new GroupsServiceImpl(getConfig());
    }

    @After
    public void tearDown() throws HodErrorException {
        for (final String name : createdGroups) {
            service.delete(getTokenProxy(), USER_STORE, name);
        }
    }

    @Test
    public void list() throws HodErrorException {
        // All we can currently say about the pre-existing groups is that their properties are not null since we don't
        // control the environment
        final List<Group> groups = service.list(getTokenProxy(), USER_STORE);
        assertThat(groups, notNullValue());

        for (final Group group : groups) {
            assertThat(group, notNullValue());
            assertThat(group.getName(), notNullValue());
        }
    }

    @Test
    public void create() throws HodErrorException {
        final NameAndResponse nameAndResponse = safeCreateGroup();
        assertThat(nameAndResponse.response.isSuccess(), is(true));
        assertThat(nameAndResponse.response.getGroupName(), is(nameAndResponse.name));
        assertThat(nameAndResponse.response.getUserStore(), is(USER_STORE));
    }

    @Test
    public void createAndList() throws HodErrorException {
        final String name = safeCreateGroup().name;
        final List<Group> groups = service.list(getTokenProxy(), USER_STORE);

        Group newGroup = null;

        for (final Group group : groups) {
            if (name.equals(group.getName())) {
                newGroup = group;
            }
        }

        assertThat(newGroup.getChildren(), is(empty()));
        assertThat(newGroup.getParents(), is(empty()));
    }

    @Test
    public void createAndDelete() throws HodErrorException {
        final String name = safeCreateGroup().name;
        final StatusResponse deleteResponse = safeDeleteGroup(name);
        assertThat(deleteResponse.isSuccess(), is(true));
    }

    @Test
    public void createDuplicate() throws HodErrorException {
        final String groupName = safeCreateGroup().name;
        HodErrorCode errorCode = null;

        try {
            service.create(getTokenProxy(), USER_STORE, groupName);
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.GROUP_ALREADY_EXISTS));
    }

    @Test
    public void createInNonExistentUserStore() {
        HodErrorCode errorCode = null;

        try {
            service.create(getTokenProxy(), new ResourceIdentifier(DOMAIN_NAME, unique()), uniqueGroupName());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.STORE_NOT_FOUND));
    }

    @Test
    public void createAndGetInfo() throws HodErrorException {
        final String groupName = safeCreateGroup().name;

        final GroupInfo info = service.getInfo(getTokenProxy(), USER_STORE, groupName);
        assertThat(info.getName(), is(groupName));
        assertThat(info.getUsers(), is(empty()));
        assertThat(info.getChildren(), is(empty()));
        assertThat(info.getParents(), is(empty()));
    }

    @Test
    public void deleteNonExistent() {
        HodErrorCode errorCode = null;

        try {
            service.delete(getTokenProxy(), USER_STORE, uniqueGroupName());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.GROUP_NOT_FOUND));
    }

    @Test
    public void assignNonExistentUser() throws HodErrorException {
        final String groupName = safeCreateGroup().name;
        HodErrorCode errorCode = null;

        try {
            service.assignUser(getTokenProxy(), USER_STORE, groupName, unique());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.USER_NOT_FOUND));
    }

    @Test
    public void removeNonExistentUser() throws HodErrorException {
        final String groupName = safeCreateGroup().name;
        HodErrorCode errorCode = null;

        try {
            service.removeUser(getTokenProxy(), USER_STORE, groupName, unique());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.USER_NOT_FOUND));
    }

    @Test
    public void assignNonExistentUserToNonExistentGroup() {
        HodErrorCode errorCode = null;

        try {
            service.assignUser(getTokenProxy(), USER_STORE, unique(), unique());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        // We don't mind if IOD checks the user or group first
        assertThat(GROUP_OR_USER_NOT_FOUND, hasItem(errorCode));
    }

    @Test
    public void removeNonExistentUserFromNonExistentGroup() {
        HodErrorCode errorCode = null;

        try {
            service.assignUser(getTokenProxy(), USER_STORE, unique(), unique());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        // We don't mind if IOD checks the user or group first
        assertThat(GROUP_OR_USER_NOT_FOUND, hasItem(errorCode));
    }

    @Test
    public void createWithParentAndGetInfo() throws HodErrorException {
        final String parent = safeCreateGroup().name;
        final String child = safeCreateGroup(Collections.singletonList(parent), null).name;
        checkGetInfoParentChildRelationship(parent, child);
    }

    @Test
    public void createWithParentAndList() throws HodErrorException {
        final String parent = safeCreateGroup().name;
        final String child = safeCreateGroup(Collections.singletonList(parent), null).name;
        checkListGroupsParentChildRelationship(child, parent);
    }

    @Test
    public void createWithTwoParentsAndList() throws HodErrorException {
        final String parent1 = safeCreateGroup().name;
        final String parent2 = safeCreateGroup().name;
        final String child = safeCreateGroup(Arrays.asList(parent1, parent2), null).name;

        final List<Group> groups = service.list(getTokenProxy(), USER_STORE);
        Group parent1Group = null;
        Group parent2Group = null;
        Group childGroup = null;

        for (final Group group : groups) {
            final String name = group.getName();

            if (parent1.equals(name)) {
                parent1Group = group;
            } else if (parent2.equals(name)) {
                parent2Group = group;
            } else if (child.equals(name)) {
                childGroup = group;
            }
        }

        assertThat(childGroup.getChildren(), is(empty()));
        assertThat(childGroup.getParents(), containsInAnyOrder(parent1, parent2));

        assertThat(parent1Group.getChildren(), contains(child));
        assertThat(parent1Group.getParents(), is(empty()));

        assertThat(parent2Group.getChildren(), contains(child));
        assertThat(parent2Group.getParents(), is(empty()));
    }

    @Test
    public void createWithChildAndGetInfo() throws HodErrorException {
        final String child = safeCreateGroup().name;
        final String parent = safeCreateGroup(null, Collections.singletonList(child)).name;
        checkGetInfoParentChildRelationship(parent, child);
    }

    @Test
    public void createWithChildAndList() throws HodErrorException {
        final String child = safeCreateGroup().name;
        final String parent = safeCreateGroup(null, Collections.singletonList(child)).name;
        checkListGroupsParentChildRelationship(child, parent);
    }

    @Test
    public void deleteParentGroup() throws HodErrorException {
        final String child = safeCreateGroup().name;
        final String parent = safeCreateGroup(null, Collections.singletonList(child)).name;
        safeDeleteGroup(parent);

        final GroupInfo childInfo = service.getInfo(getTokenProxy(), USER_STORE, child);
        assertThat(childInfo.getParents(), is(empty()));
    }

    @Test
    public void linkAndGetInfo() throws HodErrorException {
        final String child = safeCreateGroup().name;
        final String parent = safeCreateGroup().name;

        final StatusResponse link = service.link(getTokenProxy(), USER_STORE, parent, child);
        assertThat(link.isSuccess(), is(true));

        checkGetInfoParentChildRelationship(parent, child);
    }

    @Test
    public void linkNonExistentParent() throws HodErrorException {
        final String child = safeCreateGroup().name;
        HodErrorCode errorCode = null;

        try {
            service.link(getTokenProxy(), USER_STORE, unique(), child);
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.GROUP_NOT_FOUND));
    }

    @Test
    public void linkNonExistentChild() throws HodErrorException {
        final String parent = safeCreateGroup().name;
        HodErrorCode errorCode = null;

        try {
            service.link(getTokenProxy(), USER_STORE, parent, unique());
            fail("HodErrorException not thrown");
        } catch (final HodErrorException e) {
            errorCode = e.getErrorCode();
        }

        assertThat(errorCode, is(HodErrorCode.GROUP_NOT_FOUND));
    }

    // Use the get info API to check that the "parent" group is the parent of the "child", and that this is the their only relationship
    private void checkGetInfoParentChildRelationship(final String parent, final String child) throws HodErrorException {
        final GroupInfo parentInfo = service.getInfo(getTokenProxy(), USER_STORE, parent);
        final GroupInfo childInfo = service.getInfo(getTokenProxy(), USER_STORE, child);

        assertThat(parentInfo.getChildren(), contains(child));
        assertThat(parentInfo.getParents(), is(empty()));
        assertThat(childInfo.getParents(), contains(parent));
        assertThat(childInfo.getChildren(), is(empty()));
    }

    // Use the list groups API to check that the "parent" group is the parent of the "child", and that this is their only relationship
    private void checkListGroupsParentChildRelationship(final String child, final String parent) throws HodErrorException {
        final List<Group> groups = service.list(getTokenProxy(), USER_STORE);

        Group childGroup = null;
        Group parentGroup = null;

        for (final Group group : groups) {
            final String name = group.getName();

            if (parent.equals(name)) {
                parentGroup = group;
            } else if (child.equals(name)) {
                childGroup = group;
            }
        }

        assertThat(childGroup.getParents(), contains(parent));
        assertThat(parentGroup.getChildren(), contains(child));
    }

    // Create a group with a random name, storing it for deletion in tear down
    private NameAndResponse safeCreateGroup() throws HodErrorException {
        final String name = uniqueGroupName();
        final CreateGroupResponse response = service.create(getTokenProxy(), USER_STORE, name);
        createdGroups.add(name);
        return new NameAndResponse(name, response);
    }

    // Create a group with a random name and the given relationships, storing it for deletion in tear down
    private NameAndResponse safeCreateGroup(final List<String> parents, final List<String> children) throws HodErrorException {
        final String name = uniqueGroupName();
        final CreateGroupResponse response = service.createWithHierarchy(getTokenProxy(), USER_STORE, name, parents, children);
        createdGroups.add(name);
        return new NameAndResponse(name, response);
    }

    // Delete a group that was previously created, removing it from the tear down list
    private StatusResponse safeDeleteGroup(final String name) throws HodErrorException {
        if (!createdGroups.contains(name)) {
            throw new IllegalArgumentException("Attempted to safely delete group which was not created safely");
        }

        final StatusResponse response = service.delete(getTokenProxy(), USER_STORE, name);
        createdGroups.remove(name);
        return response;
    }

    private String uniqueGroupName() {
        return "java-hod-client-test-" + unique();
    }

    private String unique() {
        return UUID.randomUUID().toString();
    }

    private static class NameAndResponse {
        private final String name;
        private final CreateGroupResponse response;

        private NameAndResponse(final String name, final CreateGroupResponse response) {
            this.name = name;
            this.response = response;
        }
    }
}