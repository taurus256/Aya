create or replace function calc_duration() returns trigger
    language plpgsql
as
$$
DECLARE
BEGIN
    IF NEW.is_graph = true THEN
        IF NEW.state != 1 THEN -- это переключение ИЗ режима process в какой-то другой
			IF age(NEW.start) > interval '1 hour' THEN
				IF age(NEW.start) < interval '24 hour' THEN
					NEW.duration_h = OLD.duration_h + round(extract(hour from age(NEW.start))); -- просто добавляем количество часов
				ELSE
					NEW.duration_h = OLD.duration_h + round(extract(day from age(NEW.start)))*8; -- считаем целый день как 8 часов + добавляем оставшиеся часы
				END IF;
			END IF;
        ELSE
            NEW.start = current_timestamp;	-- если это переключение в режим process - запоминаем время
        END IF;
    END IF;

	RAISE NOTICE 'Скрипт расчета времени отработал';
    RETURN NEW;
END;
$$;

alter function calc_duration() owner to postgres;

