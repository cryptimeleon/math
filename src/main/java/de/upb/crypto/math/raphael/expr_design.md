## Requirements:
* Evaluating expression should automatically recognize opportunity for multiexponentiation and
    use efficient multiexponentiation algorithm.
* More optimization can be done in precompute. Doing too much optimization in evaluate may be
    counterproductive.
* Should allow adding future optimizations.
* Different groups allow for different exponentiation algorithms, groups where inversion is easy (like elliptic curves) can use
    a faster signed digit method like wNAF.


## Design:

`TODO`: Take a look at more example constructions for design ideas.

`TODO`: Look at jacobian vs projective coordinates also.

Sliding window exponentiation needs precomputed group elements. These can be stored in the group. Then we can use
the group as a singleton and other schemes will have access to the precomputation as well. So we need to add a
precompute method in the group that computes and stores exponentiations of the given group element.
Since exponentiation for sliding window always requires precomputation, we should do an automatic precomputation if necessary
when executing exponentiation. So then later exponentiations can reuse those group elements.

So for simple expression exponentiation:
* Evaluate automatically looks whether powers are already precomputed, else it precomputes them and stores them in
        group for later use by other algorithms. Perhaps we can add option to disregard those lookups/store operations if
        you know you will not use the precomputation, perhaps if you only make one exponentiation with that base.
* For precompute, we always do and store the precomputation.

### Multiexponentiation:

There are two methods: simultaneous and interleaved. Simultaneous precomputes combinations of powers, so it gets much
worse for more bases. Its precomputations are also not reusable for different base combinations. Interleaved still precomputes
powers for each specific base. Therefore, its precomputations can be reused for different multiexponentiations and there
are overall less precomputations. However, simultaneous – if its disatvantages do not matter – is generally faster.

For something like the verification equation of Pointcheval-Sanders 2018, the same bases from the verification key
are reused. So the multiexponentiation will always use the same bases there. This would indicate an advantage for the
simultaneous method. However, the number of bases scales with the length of the message. So for very large messages, the
number of precomputations for the simultaneous method might get too large and then we might want to use the interleaved method.
So one could argue for selecting different methods depending on message length.

`Swante Scholz's recommendation`:
Use simultaneous approach (with window size of 1 or at most 2 because otherwise precomputation consumes too much space) for number of bases not exceeding 10 with caching enabled. 
In all other cases, the interleaving method is better, with a window size of about 4 without caching, and largest window size feasible if caching is enabled.

### Configuration

Possible things to configure:
* Window size for exponentiation.
* Which exact algorithm to use for exponentiation.
* Whether caching is enabled for exponentiations and for multiexponentiation. How granular to make this? Just differentiate between single- and multiexponentiation?
* Whether to use a simultaneous or interleaved method for multiexponentiation. Also the inner exponentiation algorithm.

Default configuration:
* For multiexponentiation, use Swante's recommendations which relies on number of bases and whether caching is enabled.
* For single exponentiations, we can use wNAF in appropriate groups where inversion is easy and sliding window otherwise.
    * If caching is disabled, use window size 4 as it seems to have best results in Swante's paper.
    * If caching is enabled, use largest window size possible (need to find good specific value, infinite obviously impossible). Maybe we can look at 
        system specs to decide this. Otherwise we need to use conservative value in case of low-power devices.

How do we do configuration. So selection of window size for example. Most granular would be specifying it for each exponentiation.
However, with caching, larger window size is generally better. So most likely, user would a large window size which
does not use too much memory with the group. So perhaps we could put the configuration into the GroupElementExpressionEvaluator.
Not specifying an evaluator would then lead to default values used.
In general, all configuration can be set for the evaluator. Then the evaulator can also be reused.
One problem of this is that the evaluator is group specific. So you could not reuse the configuration for a different group.
So perhaps we need a factory class where we can set configuration and set group and then it gives us evaluator.
Then we can only ever change the things we need to. Probably not useful to put a configuration into public parameters though, since
there are still potentially large differences between, for example multiexponentiation, algorithms depending on use case.
Problem is though, that the evaluator is obtained from the group directly. Don't think it makes sense to tie configuration to group.
If you have a multiexponentiation in the same group, but one has 5 bases and the other has some arbitrary amount of bases, then
it makes no sense to use same configuration.

Keep in mind that an expression can contain multiple groups, e.g. for pairing expressions. So there might be configuration that
needs to be changed depending on group the expression evaluates to. Or e.g. there is one multiexponentiation in a source group
and then one in the target group. So one might want to use different configuration for these even though only one evaluator
would be used. Maybe one could just use different evaluators? So the user would first compute e.g. left hand multiexponentiation with some evaluator.
Then it can evaluate the PairingExpr with a different evaluator.

So for default evaluate call:
1. Calls `getExpressionEvaluator` on group. 
2. The group then instantiates an evaluator with default settings. Since there can be multiple groups in an expression, we might not want
    to set whether inversion is easy or hard here, and instead just tell the evaluator the group. Then it can make the decision which
    algorithm to use based on the group for each expression. The configuration could then just be whether to use signed digit algorithms
    if possible. In this case, getting evaluator from group makes no sense though since the expression is given to the evaluator and so
    it knows the group anyway.

For user-configuration:
* Multiexponentiation allows for a lot of configuration. For interleaved method, one could set different window sizes for different bases, for example.
    Do we want to allow for such granular configuration? You could also enable caching for e.g. singleexponentiation per basis. So if you know one basis
    has multiple single exponentiations, then you might want to enable caching just for that basis. Same for disabling.

### Concrete Design Decisions

Keep in mind we don't have to have every tiny bit of configuration available at start. Maybe just macro-scale ones to start off.

User-configuration design:
* If user wants different behaviour for different multiexponentiations, he can use multiple evaluators and just evaluate those parts of the expression
    separately. The evaluator uses the given settings for all contained multiexponentiations etc.
* Singleexponentiation: 
    * Algorithm: Can set whether to use signed digit algorithms whenever possible or to never use them. Can also specify which algorithm to use
    in either case. Regarding window size, allow specifying different window size for cached and non-cached algorithms.
    * Caching: Can set default for caching, either cache as much as is useful or completely disabled.
    Default would be to cache as much as is useful, and perhaps – since we are traversing the expression anway to detect multiexponentiations – we can disable caching if we
    see some basis is only used for one exponentiation.
* Multiexponentiation: 
    * Algorithm: User can specific whether to use interleaved or simultaneous method and the exact singleexponentiation algorithm to use in there.
    In case of interleaved, he can even specify the window size per basis. Default would be to select dynamically based on number of bases and wNAF/sliding window and to 
    select window size as per Swante's recommendations. Can also allow user to specify maximum number of bases for simultaneous. Window size should also be configurable
    depending on whether something is cached or not. 
    * Caching: Similar to singleexponentiation, just also split into simultaneous and interleaved.

Code flow for evaluate call:
1. User can either just call evaluate such that default evaluator is used or construct a user-configured evaluator and use that.
2. Evaluate then traverses the expression tree, mainly to find multiexponentiations, but it can also use this to find out whether bases have multiple exponentiations
    such that it can make a decision whether to cache. Can skip this if precompute has done this already.
3. Evalute using decision made in previous step.

Precompute call:
1. Should also be configurable what kind of precompuation is done. So add call where user can supply evaluator.
2. Do 2nd step of evaluate as well but with more optimization (should be configurable).
3. Regarding caching, we can only cache precomputed group elements in the group itself. So what about caching information about structure of expression tree?
    Stuff that 2nd step of evaluate would compute should probably be stored in the expression itself. Also whether precompute has been called so we can
    skip the 2nd step of evaluate. So 2nd step of evaluate should be modular and callable from both precompute and evalute, maybe with separate optimization 
    levels.
4. Precompute gives new expression, so store some stuff in there. Where exactly in the expression? Expression is series of nested expressions.

#### Group Singleton

Problem that remains is how to make serialization work with singleton. Better not to change `Group` to singleton but instead add a singleton precomputation
object to it. Then we can add a deserializer for it that checks whether a group with those parameters already exists and then retrieves the singleton if possible.
Problem with singleton deserialization is that you cannot reinstantiate previous singleton state since you may already have singleton in another state. In this
case, however, we don't need to reinstantiate, we can just add the state contained in the deserialized singleton to the existing one.


1. 


### Example application in constructions

For [PS18]:
* Computation of verification key requires exponentiating \tilde{g} with many different elements. There, precomputation
    for \tilde{g} is helpful. 
* Precomputation might be helpful for verification during key generation.
* When doing verification, multiexponentiation algorithm should be used.



## Material

Singleton code (thread-safe):
```Java
public class Singleton {

    private SingletonClass() {}

    private static class InstanceHolder {

        static final Singleton INSTANCE = new Singleton();

    }

    public static Singleton getInstance() {

        return InstanceHolder.INSTANCE;

    }

}
```
