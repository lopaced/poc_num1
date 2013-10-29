# Cards Library: CardList

In this page you can find info about:

* [Creating a base CardList](#creating-a-base-cardlist)
* [Use your custom layout for each row](#use-your-custom-layout-for-each-row)
* [Cards with different inner layouts](#cards-with-different-inner-layouts
* [Swipe and Undo in `CardListView`](#swipe-and-undo-in-cardlistview)


### Creating a base CardList

Creating a `CardListView` is pretty simple.

First, you need an XML layout that will display the `CardListView`.

``` xml
    <it.gmariotti.cardslib.library.view.CardListView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/myList"/>
```

Then create an Array of `Card`s:

``` java
       ArrayList<Card> cards = new ArrayList<Card>();

      //Create a Card
      Card card = new Card(getContext());

      //Create a CardHeader
      CardHeader header = new CardHeader(getContext());
      ....
      //Add Header to card
      card.addCardHeader(header);

      cards.add(card);
```

Last create a `CardArrayAdapter`, get a reference to the `CardListView` from your code and set your adapter.

``` java
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);

        CardListView listView = (CardListView) getActivity().findViewById(R.id.myList);
        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }
```

This `CardListView` uses for each row the row-list layout `res/layout/list_card_layout.xml`.


### Use your custom layout for each row

Card Library provides 2 built-in row-list layouts.

* `res/layout/list_card_layout.xml`.
* `res/layout/list_card_thumbnail_layout.xml`.

You can customize the layout used for each item in ListView using the attr: `card:list_card_layout_resourceID="@layout/my_layout`

``` xml
    <it.gmariotti.cardslib.library.view.CardListView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/carddemo_list_gplaycard"
        card:list_card_layout_resourceID="@layout/list_card_thumbnail_layout" />
```

In your row-list layout you can use your `CardView` with all its features and its possibilities.
Example `list_card_thumbnail_layout.xml`:

``` xml
    <it.gmariotti.cardslib.library.view.CardView
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:card="http://schemas.android.com/apk/res-auto"
        android:id="@+id/list_cardId"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        style="@style/list_card.thumbnail"
        card:card_layout_resourceID="@layout/card_thumbnail_layout"
        />
```

You can build your layout, but need to have:

 1. a `CardView` with the ID `list_cardId`


Currently you have to use the same inner layouts for each card in `CardListView`


![Screen](https://github.com/gabrielemariotti/cardslib/raw/master/demo/images/demo/list_gplay.png)

### Cards with different inner layouts

If you want to use cards with different inner layouts you have to:

1. set the number of different cards in your adapter with `mCardArrayAdapter.setInnerViewTypeCount`

``` java
    // Provide a custom adapter.
    // It is important to set the viewTypeCount
    // You have to provide in your card the type value with {@link Card#setType(int)} method.
    CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);
    mCardArrayAdapter.setInnerViewTypeCount(3);
```
2. set the type inside your cards with `card.setType`

``` java
    MyCard card2= new MyCard(getActivity().getApplicationContext());
    card2.setType(2); //Very important with different inner layout
``` 

You can also override the `setType` method in your `Card`.

``` java
    public class CardExample extends Card{
        @Override
        public int getType() {
            //Very important with different inner layouts
            return 2;
        }
    }
```
Moreover you can extend `CardArrayAdapter` and provide your logic.
``` java
    /**
     *  With multiple inner layouts you have to set the viewTypeCount with {@link CardArrayAdapter#setInnerViewTypeCount(int)}.
     *  </p>
     *  An alternative is to provide your CardArrayAdapter  where you have to override this method:
     *  </p>
     *  public int getViewTypeCount() {}
     *  </p>
     *  You have to provide in your card the type value with {@link Card#setType(int)} method.
     *
     */
    public class MyCardArrayAdapter extends CardArrayAdapter{

        /**
         * Constructor
         *
         * @param context The current context.
         * @param cards   The cards to represent in the ListView.
         */
        public MyCardArrayAdapter(Context context, List<Card> cards) {
            super(context, cards);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }
    }
```
You can see the example in 'ListDifferentInnerBaseFragment'.

![Screen](https://github.com/gabrielemariotti/cardslib/raw/master/demo/images/card/different_inner.png)


### Swipe and Undo in `CardListView`

If you want to enable the swipe action with an Undo Action you have to:

1. enable the swipe action on the single Cards
``` java
        //Create a Card
        Card card = new CustomCard(getActivity().getApplicationContext());

        //Enable a swipe action
        card.setSwipeable(true);
```
2. provide a id for each card
``` java
        card.setId("xxxx");
```
3. enable the undo action on the List
``` java
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);

        //Enable undo controller!
        mCardArrayAdapter.setEnableUndo(true);
 ```
4. include the undo bar in your layout. You can use the build-in layout `res/layout/list_card_undo_message.xml'.
``` xml
  <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
               xmlns:card="http://schemas.android.com/apk/res-auto"
               android:layout_width="match_parent"
               android:layout_height="match_parent">

      <RelativeLayout
          android:layout_width="match_parent"
          android:layout_height="match_parent">

          <!-- You can customize this layout.
           You need to have in your layout a `CardView` with the ID `list_cardId` -->
          <it.gmariotti.cardslib.library.view.CardListView
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:id="@+id/carddemo_list_gplaycard"
              card:list_card_layout_resourceID="@layout/list_card_thumbnail_layout"/>

      </RelativeLayout>

      <!-- Include undo message layout -->
      <include layout="@layout/list_card_undo_message"/>

  </FrameLayout>
```

It is not mandatory. You can set a `Card.OnSwipeListener` to listen the swipe action.

``` java
        //You can set a SwipeListener.
        card.setOnSwipeListener(new Card.OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                //Do something
            }
        });
```

Then you can set a `Card.OnUndoSwipeListListener` to listen the undo action.

``` java
            card.setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
                @Override
                public void onUndoSwipe(Card card) {
                    //Do something
                }
            });
```

You can customize the undo bar. The easiest way is to copy the styles inside `res/values/styles_undo.xml` in your project.

You can see the example in `ListGplayUndoCardFragment`.

![Screen](https://github.com/gabrielemariotti/cardslib/raw/master/demo/images/card/cardWithUndo.png)
