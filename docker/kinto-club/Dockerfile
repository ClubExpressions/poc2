FROM kinto/kinto-server

ENV KINTO_AUTH0_PLUGIN_VERSION=c5f0462

# Uncomment for pluhin development
#COPY kinto-auth0-openid-connect /kinto-auth0-openid-connect
#RUN pip3 install /kinto-auth0-openid-connect

RUN pip3 install https://codeload.github.com/ClubExpressions/kinto-auth0-openid-connect/zip/${KINTO_AUTH0_PLUGIN_VERSION}
