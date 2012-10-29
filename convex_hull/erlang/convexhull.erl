-module(convexhull).
-compile(export_all).

-record(point, {x, y}).
-record(vector, {x, y}).

-define(BASE, #point{x=0, y=0}).

distance(#point{x=X1, y=Y1}, #point{x=X2, y=Y2}) ->
	math:sqrt(sqr(X2-X1) + sqr(Y2-Y1)).	

sqr(X) ->
	X*X.

move(P = #point{x=X, y=Y}, Dx, Dy) ->
	P#point{x=(X+Dx), y=(Y+Dy)}.

vector_from_points(#point{x=X1, y=Y1}, #point{x=X2, y=Y2}) ->
	#vector{x=(X2-X1), y=(Y2-Y1)}.

pseudoscalar_product(#vector{x=X1, y=Y1}, #vector{x=X2, y=Y2}) ->
	((X1*Y2) - (X2*Y1)).

find_left_least([First | Rest]) ->
	find_left_least(Rest, First).
find_left_least([], CurrLeftLeast) ->
	CurrLeftLeast;
find_left_least([H = #point{y=Y} | T], #point{y=CurrY}) when Y < CurrY ->
	find_left_least(T, H);
find_left_least([H = #point{x=X, y=SameY} | T], #point{x=CurrX, y=SameY}) when X < CurrX ->
	find_left_least(T, H);
find_left_least([_ | T], CurrLeftLeast) ->
	find_left_least(T, CurrLeftLeast).

remove_point(Points, PointToRemove) ->
	[P || P <- Points, P /= PointToRemove].

move_points(Points, Dx, Dy) ->
	[move(P, Dx, Dy) || P <- Points].

compare_by_polar_angle(P1, P2) ->
	V1 = vector_from_points(?BASE, P1),
	V2 = vector_from_points(?BASE, P2),
	Prod = pseudoscalar_product(V1, V2),
	case Prod == 0 of
		true ->
			Dist1 = distance(?BASE, P1),
			Dist2 = distance(?BASE, P2),
			Dist1 =< Dist2;
		false ->
			Prod > 0			
	end.

build(Points) when length(Points) >= 3 ->
	LeftLeast = find_left_least(Points),
	RemainderPoints = remove_point(Points, LeftLeast),
	NormalizedPoints = move_points(RemainderPoints, -LeftLeast#point.x, -LeftLeast#point.y),
	SortedPoints = lists:sort(fun ?MODULE:compare_by_polar_angle/2, NormalizedPoints),
	[P1, P2 | Rest] = SortedPoints,
	ConvexHull = create_convex_hull(Rest, [P2, P1, ?BASE]),
	move_points(ConvexHull, LeftLeast#point.x, LeftLeast#point.y).

create_convex_hull([], ConvexHull) ->
	ConvexHull;
create_convex_hull([Pc | T], [Pb, Pa | Tch] = ConvexHull) when length(ConvexHull) > 2 ->
	Vab = vector_from_points(Pa, Pb),
	Vbc = vector_from_points(Pb, Pc),
	Prod = pseudoscalar_product(Vab, Vbc),
	case Prod > 0 of
		true ->
			create_convex_hull(T, [Pc | ConvexHull]);
		false ->
			create_convex_hull([Pc | T], [Pa | Tch])
	end;
create_convex_hull([Pc | T], ConvexHull) ->
	create_convex_hull(T, [Pc | ConvexHull]).
