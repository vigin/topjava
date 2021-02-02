package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();

        meals.forEach(userMeal -> caloriesPerDays.merge(userMeal.getDateTime().toLocalDate(), userMeal.getCalories(), Integer::sum));

        List<UserMealWithExcess> result = new ArrayList<>();
        meals.forEach(userMeal -> {
            if (LocalTime.from(userMeal.getDateTime()).isAfter(startTime) && LocalTime.from(userMeal.getDateTime()).isBefore(endTime)) {
                result.add(new UserMealWithExcess(userMeal,
                        caloriesPerDays.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        });

        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Predicate<UserMeal> predicateStartTime = m -> LocalTime.from(m.getDateTime()).isAfter(startTime);
        Predicate<UserMeal> predicateEndTime = m -> LocalTime.from(m.getDateTime()).isBefore(endTime);
        Predicate<UserMeal> predicateBetweenStartTimeAndEndTime = predicateStartTime.and(predicateEndTime);

        //сгрупировать все по ключу дня
        Map<LocalDate, Integer> caloriesPerDays = meals.stream()
                .collect(Collectors.groupingBy((p) -> LocalDate.from(p.getDateTime()),
                                Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(predicateBetweenStartTimeAndEndTime)
                .map(meal -> new UserMealWithExcess(meal,
                        caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }
}
