/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.hod.client.api.userstore.group;

import com.hp.autonomy.hod.client.api.authentication.AuthenticationToken;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.userstore.StatusResponse;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.config.Requester;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.client.util.MultiMap;
import retrofit.client.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupsServiceImpl implements GroupsService {
    private final GroupsBackend backend;
    private final Requester requester;

    public GroupsServiceImpl(final HodServiceConfig config) {
        backend = config.getRestAdapter().create(GroupsBackend.class);
        requester = config.getRequester();
    }

    @Override
    public List<Group> list(final ResourceIdentifier userStore) throws HodErrorException {
        return requester.makeRequest(ListGroupsResponse.class, listBackendCaller(userStore)).getGroups();
    }

    @Override
    public List<Group> list(final TokenProxy tokenProxy, final ResourceIdentifier userStore) throws HodErrorException {
        return requester.makeRequest(tokenProxy, ListGroupsResponse.class, listBackendCaller(userStore)).getGroups();
    }

    private Requester.BackendCaller listBackendCaller(final ResourceIdentifier userStore) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.list(userStore, token);
            }
        };
    }

    @Override
    public GroupInfo getInfo(final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(GroupInfo.class, getInfoBackendCaller(userStore, name));
    }

    @Override
    public GroupInfo getInfo(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(tokenProxy, GroupInfo.class, getInfoBackendCaller(userStore, name));
    }

    private Requester.BackendCaller getInfoBackendCaller(final ResourceIdentifier userStore, final String group) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.getInfo(userStore, group, token);
            }
        };
    }

    @Override
    public CreateGroupResponse create(final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(CreateGroupResponse.class, createBackendCaller(userStore, Collections.<String, Object>emptyMap(), name));
    }

    @Override
    public CreateGroupResponse create(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(tokenProxy, CreateGroupResponse.class, createBackendCaller(userStore, Collections.<String, Object>emptyMap(), name));
    }

    @Override
    public CreateGroupResponse createWithHierarchy(final ResourceIdentifier userStore, final String name, final List<String> parents, final List<String> children) throws HodErrorException {
        return requester.makeRequest(CreateGroupResponse.class, createBackendCaller(userStore, buildHierarchyParameters(parents, children), name));
    }

    @Override
    public CreateGroupResponse createWithHierarchy(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String name, final List<String> parents, final List<String> children) throws HodErrorException {
        return requester.makeRequest(tokenProxy, CreateGroupResponse.class, createBackendCaller(userStore, buildHierarchyParameters(parents, children), name));
    }

    private Map<String, Object> buildHierarchyParameters(final List<String> parents, final List<String> children) {
        final MultiMap<String, Object> parameters = new MultiMap<>();

        if (parents != null) {
            for (final String parent : parents) {
                parameters.put("parents", parent);
            }
        }

        if (children != null) {
            for (final String child : children) {
                parameters.put("children", child);
            }
        }

        return parameters;
    }

    private Requester.BackendCaller createBackendCaller(final ResourceIdentifier userStore, final Map<String, Object> hierarchyParameters, final String group) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.create(userStore, group, hierarchyParameters, token);
            }
        };
    }

    @Override
    public StatusResponse delete(final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(StatusResponse.class, deleteBackendCaller(userStore, name));
    }

    @Override
    public StatusResponse delete(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String name) throws HodErrorException {
        return requester.makeRequest(tokenProxy, StatusResponse.class, deleteBackendCaller(userStore, name));
    }

    private Requester.BackendCaller deleteBackendCaller(final ResourceIdentifier userStore, final String group) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.delete(userStore, group, token);
            }
        };
    }

    @Override
    public AssignUserResponse assignUser(final ResourceIdentifier userStore, final String groupName, final String userUuid) throws HodErrorException {
        return requester.makeRequest(AssignUserResponse.class, assignBackendCaller(userStore, groupName, userUuid));
    }

    @Override
    public AssignUserResponse assignUser(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String groupName, final String userUuid) throws HodErrorException {
        return requester.makeRequest(tokenProxy, AssignUserResponse.class, assignBackendCaller(userStore, groupName, userUuid));
    }

    private Requester.BackendCaller assignBackendCaller(final ResourceIdentifier userStore, final String group, final String userUuid) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.assignUser(userStore, group, userUuid, token);
            }
        };
    }

    @Override
    public StatusResponse removeUser(final ResourceIdentifier userStore, final String groupName, final String userUuid) throws HodErrorException {
        return requester.makeRequest(StatusResponse.class, removeBackendCaller(userStore, groupName, userUuid));
    }

    @Override
    public StatusResponse removeUser(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String groupName, final String userUuid) throws HodErrorException {
        return requester.makeRequest(tokenProxy, StatusResponse.class, removeBackendCaller(userStore, groupName, userUuid));
    }

    private Requester.BackendCaller removeBackendCaller(final ResourceIdentifier userStore, final String group, final String userUuid) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.removeUser(userStore, group, userUuid, token);
            }
        };
    }

    @Override
    public StatusResponse link(final ResourceIdentifier userStore, final String parent, final String child) throws HodErrorException {
        return requester.makeRequest(StatusResponse.class, linkBackendCaller(userStore, parent, child));
    }

    @Override
    public StatusResponse link(final TokenProxy tokenProxy, final ResourceIdentifier userStore, final String parent, final String child) throws HodErrorException {
        return requester.makeRequest(tokenProxy, StatusResponse.class, linkBackendCaller(userStore, parent, child));
    }

    private Requester.BackendCaller linkBackendCaller(final ResourceIdentifier userStore, final String parent, final String child) {
        return new Requester.BackendCaller() {
            @Override
            public Response makeRequest(final AuthenticationToken token) throws HodErrorException {
                return backend.link(userStore, parent, child, token);
            }
        };
    }
}
