package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.r2dbc.entities.UserEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        String,
        UserReactiveRepository
        > implements UserRepository {
    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.mapBuilder(d, User.UserBuilder.class).build());
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }


}
